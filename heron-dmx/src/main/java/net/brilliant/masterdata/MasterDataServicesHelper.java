/**
 * 
 */
package net.brilliant.masterdata;

/**
 * @author ducbq
 *
 */
/*
@Component
public class MasterDataServicesHelper extends CompCore {
	private static final long serialVersionUID = 5995924041891192202L;

	@Inject
	private LanguageService languageService;

	@Inject
	private LingualHelper lingualHelper;

	public void initialize() {
		initLanguages();
		initCountries();
		initBasicConfigs();
	}

	private void initBasicConfigs() {
		
	}

	private void initLanguages() {
		log.info("Enter languagues intialize");
		try {
			if (1 > this.languageService.count("code", DmxConstants.LANGUAGE_CODE_VIETNAM)) {
				this.languageService.saveOrUpdate(Language.builder().code(DmxConstants.LANGUAGE_CODE_VIETNAM).name("Vietnamese").build());
			}

			if (1 > this.languageService.count("code", DmxConstants.LANGUAGE_CODE_ENGLISH)) {
				this.languageService.saveOrUpdate(Language.builder().code(DmxConstants.LANGUAGE_CODE_ENGLISH).name("English").build());
			}
		} catch (Exception e) {
			log.error(e);
		}
		log.info("Leave languagues intialize");
	}

	private void initCountries() {
		log.info("Enter countries intialize");
		this.lingualHelper.initAvailableCountries();
		log.info("Leave countries intialize");
	}
}
*/