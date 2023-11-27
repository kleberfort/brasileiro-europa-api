package br.com.kfs.brasileiraoeuropaapi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.kfs.brasileiraoeuropaapi.dto.PartidaGoogleDTO;

public class ScrapingUtil {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String  BASE_URL_GOOGLE = "https://www.google.com/search?q=";
	private static final String  COMPLEMENTO_URL_GOOGLE = "&hl=pt_BR";

	public static void main(String[] args) {
		
		String url = BASE_URL_GOOGLE + "brasil+x+argentina" + COMPLEMENTO_URL_GOOGLE;
		
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
			
			if(statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
				String tempoPartida = obtemTempoPartida(document);
				LOGGER.info("Tempo partida: {}", tempoPartida);  
				}
			
			String nomeEquipeCasa = recuperaNomeEquipeCasa(document);
			LOGGER.info("Nome Equipe Casa: {}", nomeEquipeCasa);
			
			String nomeEquipeVisitante = recuperaNomeEquipeVisitante(document);
			LOGGER.info("Nome Equipe Visitante: {}", nomeEquipeVisitante);
			
			
			Integer placarEquipeCasa = recuperaPlacarEquipeCasa(document);
			LOGGER.info("Placar Equipe Casa: {}", placarEquipeCasa);
			
            Integer placarEquipeVisitante = recuperaPlacarEquipeVisitante(document);
            LOGGER.info("Placar Equipe Visitange: {}", placarEquipeVisitante);

            String golsEquipeCasa = recuperaGolsEquipeCasa(document);
            System.out.println("Gols Equipe Casa: " + golsEquipeCasa);

            String golsEquipeVisitante = recuperaGolsEquipeVisitante(document);
            System.out.println("Gols Equipe Visitante: " +golsEquipeVisitante);
			
			/*
			 * //erro Ao recuperar a logo das partidas String urlLogoEquipeCasa =
			 * recuperaLogoEquipeCasa(document); LOGGER.info("Logo Equipe casa: {}",
			 * urlLogoEquipeCasa);
			 */
			
			
		
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
	
	public String recuperaNomeEquipeCasa(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String nomeEquipe = elemento.select("span").text();
		return nomeEquipe;
	}
	
	public String recuperaNomeEquipeVisitante(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]");
		String nomeEquipe = elemento.select("span").text();
		return nomeEquipe;
	}
	
	   public static Integer recuperaPlacarEquipeCasa(Document document){

           String placarEquipe = document.selectFirst("div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]").text();
           return Integer.valueOf(placarEquipe);

  }

   public static Integer recuperaPlacarEquipeVisitante(Document document){
       String placarEquipe = document.selectFirst("div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]").text();
       return Integer.valueOf(placarEquipe);
   }
	
   
   public static String recuperaGolsEquipeCasa(Document document){
       List<String> golsEquipe = new ArrayList<>();
       Elements elementos = document.select("div[class=imso_gs__tgs imso_gs__left-team]").select("div[class=imso_gs__gs-r]");

       for (Element e: elementos) {
           String infoGols = e.select("div[class=imso_gs__gs-r]").text();
           golsEquipe.add(infoGols);
       }

       return String.join(", ", golsEquipe);
   }

   public static String recuperaGolsEquipeVisitante(Document document){
       List<String> golsEquipe = new ArrayList<>();
       Elements elemento = document.select("div[class=imso_gs__tgs imso_gs__right-team]").select("div[class=imso_gs__gs-r]");
       for (Element e : elemento) {
           String infGols = e.select("div[class=imso_gs__gs-r]").text();
           golsEquipe.add(infGols);
       }

       return String.join(", ", golsEquipe);

   }
	

	/*
	 * public String recuperaLogoEquipeCasa(Document document){ Element elemento =
	 * document.
	 * selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-loa imso-ut imso-tnol]"
	 * ); String urlLogo =
	 * elemento.select("img[class=imso_btl__mh-logo]").attr("src");
	 * 
	 * return urlLogo; }
	 */
	
	
	
	
	

}
