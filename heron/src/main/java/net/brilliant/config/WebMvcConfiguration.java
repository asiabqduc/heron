/**
 * 
 */
package net.brilliant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ducbq
 *
 */
@Configuration
@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {
	/*@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(CommonUtility.LOCALE_VIETNAMESE); // change this
		return localeResolver;
	}*/

  @Bean
  public PasswordEncoder passwordEncoder() {
  	//return encryptoReporistory.getSCryptPasswordEncoder();
  	return new BCryptPasswordEncoder();/*virtualPasswordEncoder*/
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // TODO Auto-generated method stub
    registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    WebMvcConfigurer.super.addResourceHandlers(registry);
  }
}
