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
		
		String url = BASE_URL_GOOGLE + "guiana+francesa+x+bermuda" + COMPLEMENTO_URL_GOOGLE;
		
		ScrapingUtil scraping = new ScrapingUtil();
		scraping.obtemInformacoespartida(url);

	}//fim do main
	
	public PartidaGoogleDTO obtemInformacoespartida(String url) {
		PartidaGoogleDTO partida = new PartidaGoogleDTO();
		
		Document document = null;
		
		
		try {
			document = Jsoup.connect(url).get();
			
			String title = document.title();
			LOGGER.info("Titulo da pagina: {}", title);
			
			StatusPartida statusPartida = obtemStatusPartida(document);
			LOGGER.info("Status partida: {}", statusPartida);
			
			String tempoPartida = obtemTempoPartida(document);
			LOGGER.info("Tempo partida: {}", tempoPartida);
		
		} catch (IOException e) {
			LOGGER.error("ERRO AO TENTAR CONECTAR NO GOGOLE COM JSOUP -> {}", 	e.getMessage());
			
		}
		
		
		
		return partida;
	}
	
	public StatusPartida obtemStatusPartida(Document document) {
		//Situações
		// 1 - partida nao iniciada
		// 2 - partida iniciada/jogo rolando/intervalo
		// 3 - partida encerrada
		// 4 - penalidades
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		
		boolean isTempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").isEmpty();
		
		if(!isTempoPartida) {
			String tempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			if(tempoPartida.contains("Pênaltis")) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
			
			//LOGGER.info(tempoPartida);
			
		}
		
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if(!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
		
		//LOGGER.info(statusPartida.toString());
		return statusPartida;
		
		
	}
	
	public String obtemTempoPartida(Document document) {
		String tempoPartida = null;
		
		//jogo rolando ou intervalo ou penaltis
		boolean isTempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").isEmpty();
		if(!isTempoPartida) {
			tempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").first().text();
		}
		
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if(!isTempoPartida) {
			tempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").first().text();
		}
		
		
		
		//LOGGER.info(corrigeTempoPartida(tempoPartida));
		return corrigeTempoPartida(tempoPartida);
	}
	
	public String corrigeTempoPartida(String tempo) {
		if(tempo.contains("'")) {
			return tempo.replace(" ", "").replace("'", " min");
		}else {
			return tempo;
		}
		
		
		
	}
	
	

}
