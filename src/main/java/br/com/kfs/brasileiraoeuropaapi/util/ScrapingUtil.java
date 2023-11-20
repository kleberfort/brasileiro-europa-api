package br.com.kfs.brasileiraoeuropaapi.util;

import java.io.IOException;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.kfs.brasileiraoeuropaapi.dto.PartidaGoogleDTO;

public class ScrapingUtil {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String  BASE_URL_GOOGLE = "https://www.google.com/search?q=";
	private static final String  COMPLEMENTO_URL_GOOGLE = "&hl=pt_BR";

	public static void main(String[] args) {
		
		String url = BASE_URL_GOOGLE + "fortaleza+x+cruzeiro+18/11/2023" + COMPLEMENTO_URL_GOOGLE;
		
		ScrapingUtil scraping = new ScrapingUtil();
		scraping.obtemInformacoespartida(url);

	}
	
	public PartidaGoogleDTO obtemInformacoespartida(String url) {
		PartidaGoogleDTO partida = new PartidaGoogleDTO();
		
		Document document = null;
		
		
		try {
			document = Jsoup.connect(url).get();
			
			String title = document.title();
			LOGGER.info("Titulo da pagina: {}", title);
			
			
		} catch (IOException e) {
			LOGGER.error("ERRO AO TENTAR CONECTAR NO GOGOLE COM JSOUP -> {}", 	e.getMessage());
			
		}
		
		
		
		return partida;
	}
	

}
