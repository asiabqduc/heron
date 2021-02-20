/**
 * 
 */
package net.brilliant.osx.helper;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.osx.model.OSXWorkbook;
import net.brilliant.osx.model.OSXWorksheet;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OsxBucketContainer;

/**
 * @author bqduc
 *
 */
@SuppressWarnings({ "unused", "unchecked" })
public class XSSFEventDataHelper {
	public static final String MISSING_ENTRY = "";
	List<List<String>> stringTable = null;
	List<String> sbCurrentRow = null;
	int actualMaxPhysicalCells = 0;

	public static int defaultNumberOfCells = 500;

	/***---------Begin of buffered sheet content handler---------***/
	private class BufferedSheetContentsHandler implements SheetContentsHandler {
		private boolean firstCellOfRow = false;
		private int currentRow = -1;
		private int currentCol = -1;

		private void outputMissingRows(int number) {
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < limitedColumns; j++) {
					sbCurrentRow.add(MISSING_ENTRY);
				}
			}
		}

		@Override
		public void startRow(int rowNum) {
			//Initialize the 
			sbCurrentRow = CollectionsUtility.createArrayList();

			// If there were gaps, output the missing rows
			outputMissingRows(rowNum - currentRow - 1);
			// Prepare for this row
			firstCellOfRow = true;
			currentRow = rowNum;
			currentCol = -1;
		}

		@Override
		public void endRow(int rowNum) {
			// Ensure the minimum number of columns
			for (int i = currentCol; i < limitedColumns; i++) {
				sbCurrentRow.add(MISSING_ENTRY);
			}
			if (CollectionsUtility.isNotEmptyList(sbCurrentRow)){
				stringTable.add(sbCurrentRow);
			}
		}

		@Override
		public void cell(String cellReference, String formattedValue, XSSFComment comment) {
			if (firstCellOfRow) {
				firstCellOfRow = false;
			}

			// gracefully handle missing CellRef here in a similar way as XSSFCell does
			if (cellReference == null) {
				cellReference = new CellAddress(currentRow, currentCol).formatAsString();
			}

			// Did we miss any cells?
			int thisCol = (new CellReference(cellReference)).getCol();
			int missedCols = thisCol - currentCol - 1;
			
			//Evaluate actual maximum number of cells
			if (thisCol > actualMaxPhysicalCells){
				actualMaxPhysicalCells = thisCol;
			}

			for (int i = 0; i < missedCols; i++) {
				sbCurrentRow.add(MISSING_ENTRY);
			}
			currentCol = thisCol;

			// Number or string?
			try {
				// no inspection ResultOfMethodCallIgnored
				Double.parseDouble(formattedValue);
				sbCurrentRow.add(formattedValue);
			} catch (NumberFormatException e) {
				sbCurrentRow.add(formattedValue);
			}
		}

		@Override
		public void headerFooter(String text, boolean isHeader, String tagName) {
			// Skip, no headers or footers in CSV
		}
	}
	/***---------End of buffered sheet content handler---------***/

	///////////////////////////////////////

	private final OPCPackage xlsxPackage;

	/**
	 * Number of columns to read starting with leftmost
	 */
	private final int limitedColumns;
	private final Map<Object, Object> parameters;
	
	/**
	 * Creates a new XLSX -> CSV converter
	 *
	 * @param pkg
	 *          The XLSX package to process
	 *          
	 * @param minColumns
	 *          The minimum number of columns to output, or -1 for no minimum
	 */
	private XSSFEventDataHelper(OPCPackage pkg, Map<?, ?> params) {
		this.xlsxPackage = pkg;
		this.parameters = CollectionsUtility.createMap();
		this.parameters.putAll(params);

		int procLimitedColumns = defaultNumberOfCells;
		if (this.parameters.containsKey(OSXConstants.LIMITED_COLUMNS)) {
			procLimitedColumns = (Integer)this.parameters.get(OSXConstants.LIMITED_COLUMNS);
		}
		this.limitedColumns = procLimitedColumns;
	}

	public static XSSFEventDataHelper instance(Map<?, ?> params) throws CerberusException {
		if (!params.containsKey(OSXConstants.INPUT_STREAM))
			throw new CerberusException("No input stream parameter!");

		OPCPackage opcPackage = null;
		InputStream decryptedDataStream = null;
		POIFSFileSystem filesystem = null;
		EncryptionInfo encryptionInfo = null;
		Decryptor decryptor = null;
		try {
			if (params.containsKey(OSXConstants.ENCRYPTION_KEYS)) {
				filesystem = new POIFSFileSystem((InputStream) params.get(OSXConstants.INPUT_STREAM));
				encryptionInfo = new EncryptionInfo(filesystem);
				decryptor = Decryptor.getInstance(encryptionInfo);

				try {
				    if (!decryptor.verifyPassword((String)params.get(OSXConstants.ENCRYPTION_KEYS))) {
				        throw new RuntimeException("Unable to process: document is encrypted");
				    }

				    decryptedDataStream = decryptor.getDataStream(filesystem);
				} catch (GeneralSecurityException ex) {
				    throw new CerberusException("Unable to process encrypted document", ex);
				}
				opcPackage = OPCPackage.open(decryptedDataStream);
			} else {
				opcPackage = OPCPackage.open((InputStream)params.get(OSXConstants.INPUT_STREAM));
			}
		} catch (Exception e) {
			throw new CerberusException(e);
		}
		return new XSSFEventDataHelper(opcPackage, params);
	}

	/**
	 * Parses and shows the content of one sheet using the specified styles and shared-strings tables.
	 *
	 * @param styles
	 *          The table of styles that may be referenced by cells in the sheet
	 * @param strings
	 *          The table of strings that may be referenced by cells in the sheet
	 * @param sheetInputStream
	 *          The stream to read the sheet-data from.
	 * 
	 * @exception java.io.IOException
	 *              An IO exception from the parser, possibly from a byte stream or character stream supplied by the application.
	 * @throws SAXException
	 *           if parsing the XML data fails.
	 */
	public void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, SheetContentsHandler sheetHandler, InputStream sheetInputStream)
			throws IOException, SAXException {
		DataFormatter formatter = new DataFormatter();
		InputSource sheetSource = new InputSource(sheetInputStream);
		XMLReader sheetParserEx = null;
		try {
			this.actualMaxPhysicalCells = 0;
			/*SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxFactory.newSAXParser();
      XMLReader sheetParser = saxParser.getXMLReader();*/
			sheetParserEx = SAXHelper.newXMLReader();
			ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, sheetHandler, formatter, false);
			sheetParserEx.setContentHandler(handler);
			sheetParserEx.parse(sheetSource);
		} catch (Exception e) {
			throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
		}
	}

	public int getActualMaxPhysicalCells() {
		return actualMaxPhysicalCells;
	}

	public OsxBucketContainer extractData(List<String> sheets, Map<Object, Object> configParams) throws InvalidOperationException, IOException, OpenXML4JException, SAXException{
		InputStream stream = null;
		OsxBucketContainer dataBucket = OsxBucketContainer.instance();
		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
		XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
		StylesTable styles = xssfReader.getStylesTable();
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		SheetContentsHandler sheetContentsHandler = new BufferedSheetContentsHandler();
		String rowIndexKey = "";
		Integer startedRowIndex = null;
		while (iter.hasNext()) {
			stream = iter.next();
			if (sheets.contains(iter.getSheetName())){
				this.stringTable = CollectionsUtility.createArrayList();
				//Process the started row index 
				rowIndexKey = iter.getSheetName() + OSXConstants.STARTED_ROW_INDEX;
				if (null != configParams && configParams.containsKey(rowIndexKey)){
					startedRowIndex = (Integer)configParams.get(rowIndexKey);
				}

				if (null==startedRowIndex) {
					startedRowIndex = 0;
				}

				processSheet(styles, strings, sheetContentsHandler, stream);
				for (int idx = 0; idx < startedRowIndex; ++idx){
					this.stringTable.remove(0);
				}
				dataBucket.put(iter.getSheetName(), this.stringTable);
			}
			stream.close();
		}
		this.xlsxPackage.close();
		return dataBucket;
	}

	private OsxBucketContainer extractXlsxData() throws CerberusException {
		List<String> sheets = (List<String>)this.parameters.get(OSXConstants.PROCESSING_DATASHEET_IDS);
		AesZipFileZipEntrySource aesZipFileZipEntrySource = null;
		InputStream stream = null;
		OsxBucketContainer dataBucket = OsxBucketContainer.instance();
		try {
			if (this.parameters.containsKey(OSXConstants.ENCRYPTION_KEYS)) {
				aesZipFileZipEntrySource = AesZipFileZipEntrySource.createZipEntrySource((InputStream) this.parameters.get(OSXConstants.INPUT_STREAM));
			}

			ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
			XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
			StylesTable styles = xssfReader.getStylesTable();
			XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
			SheetContentsHandler sheetContentsHandler = new BufferedSheetContentsHandler();
			String rowIndexKey = "";
			Integer startedRowIndex = null;
			while (iter.hasNext()) {
				stream = iter.next();
				if (sheets.contains(iter.getSheetName())){
					this.stringTable = CollectionsUtility.createArrayList();
					//Process the started row index 
					rowIndexKey = iter.getSheetName() + OSXConstants.STARTED_ROW_INDEX;
					if (this.parameters.containsKey(rowIndexKey)){
						startedRowIndex = (Integer)this.parameters.get(rowIndexKey);
					}

					if (null==startedRowIndex) {
						startedRowIndex = 0;
					}

					processSheet(styles, strings, sheetContentsHandler, stream);
					for (int idx = 0; idx < startedRowIndex; ++idx){
						this.stringTable.remove(0);
					}
					dataBucket.put(iter.getSheetName(), this.stringTable);
				}
				stream.close();
			}
			this.xlsxPackage.close();
		} catch (Exception e) {
			throw new CerberusException(e);
		}

		return dataBucket;
	}

	public OsxBucketContainer parseXlsxData() throws CerberusException {
		InputStream inputStream = null;
		String sheetName = null;
		OsxBucketContainer dataBucket = OsxBucketContainer.instance();
		ReadOnlySharedStringsTable sharedStringTable = null;
		XSSFReader xssfReader = null;
		StylesTable styles = null;
		XSSFReader.SheetIterator iter = null;
		SheetContentsHandler sheetContentsHandler = null;
		List<String> sheetIds = (List<String>)this.parameters.get(OSXConstants.PROCESSING_DATASHEET_IDS);
		String rowIndexKey = null;
		Integer startedRowIndex = null;
		try {
			sharedStringTable = new ReadOnlySharedStringsTable(this.xlsxPackage);
			xssfReader = new XSSFReader(this.xlsxPackage);
			styles = xssfReader.getStylesTable();
			iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
			sheetContentsHandler = new BufferedSheetContentsHandler();
			while (iter.hasNext()) {
				this.stringTable = CollectionsUtility.createArrayList();
				inputStream = iter.next();
				if (!sheetIds.contains(iter.getSheetName()))
					continue;

				sheetName = iter.getSheetName();
				rowIndexKey = iter.getSheetName() + OSXConstants.STARTED_ROW_INDEX;
				if (this.parameters.containsKey(rowIndexKey)){
					startedRowIndex = (Integer)this.parameters.get(rowIndexKey);
				}

				if (null==startedRowIndex) {
					startedRowIndex = 0;
				}

				processSheet(styles, sharedStringTable, sheetContentsHandler, inputStream);
				for (int idx = 0; idx < startedRowIndex; ++idx){
					this.stringTable.remove(0);
				}
				dataBucket.put(sheetName, this.stringTable);
				inputStream.close();
			}
			this.xlsxPackage.close();
		} catch (Exception e) {
			throw new CerberusException(e);
		}
		return dataBucket;
	}

	public OSXWorkbook readXlsx() throws CerberusException {
		InputStream inputStream = null;
		String sheetName = null;
		ReadOnlySharedStringsTable sharedStringTable = null;
		XSSFReader xssfReader = null;
		StylesTable styles = null;
		XSSFReader.SheetIterator iter = null;
		SheetContentsHandler sheetContentsHandler = null;
		List<String> sheetIds = (List<String>)this.parameters.get(OSXConstants.PROCESSING_DATASHEET_IDS);
		String rowIndexKey = null;
		Integer startedRowIndex = null;
		OSXWorksheet worksheetContainer = null;
		OSXWorkbook workbookContainer = null;
		List<String> dataRows = null;
		try {
			workbookContainer = OSXWorkbook.builder().build();

			sharedStringTable = new ReadOnlySharedStringsTable(this.xlsxPackage);
			xssfReader = new XSSFReader(this.xlsxPackage);
			styles = xssfReader.getStylesTable();
			iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
			sheetContentsHandler = new BufferedSheetContentsHandler();
			while (iter.hasNext()) {
				this.stringTable = CollectionsUtility.createArrayList();
				inputStream = iter.next();
				if (!sheetIds.contains(iter.getSheetName()))
					continue;

				sheetName = iter.getSheetName();
				worksheetContainer = OSXWorksheet.builder()
						.id(sheetName)
						.build();

				rowIndexKey = iter.getSheetName() + OSXConstants.STARTED_ROW_INDEX;
				if (this.parameters.containsKey(rowIndexKey)){
					startedRowIndex = (Integer)this.parameters.get(rowIndexKey);
				}

				if (null==startedRowIndex) {
					startedRowIndex = 0;
				}

				processSheet(styles, sharedStringTable, sheetContentsHandler, inputStream);
				for (int idx = 0; idx < startedRowIndex; ++idx){
					this.stringTable.remove(0);
				}

				for (int idx=0; idx < this.stringTable.size(); idx++) {
					dataRows = this.stringTable.get(idx);
					worksheetContainer.addDataRows(idx, dataRows);
				}
				workbookContainer.put(sheetName, worksheetContainer);
				inputStream.close();
			}
			this.xlsxPackage.close();
		} catch (Exception e) {
			throw new CerberusException(e);
		}
		return workbookContainer;
	}
}
