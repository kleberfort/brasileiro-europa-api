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
	
	private static final String DIV_PARTIDA_ANDAMENTO = "div[class=imso_mh__lv-m-stts-cont]";
	private static final String DIV_PARTIDA_ENCERRADA = "span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]";
	
	private static final String DIV_NOME_EQUIPE_CASA = "div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String DIV_NOME_EQUIPE_VISITANTE = "div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]";
	
	private static final String DIV_PLACAR_EQUIPE_CASA = "div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String DIV_PLACAR_EQUIPE_VISITANTE = "div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]";
	
	private static final String DIV_GOLS_EQUIPE_CASA = "div[class=imso_gs__tgs imso_gs__left-team]";
	private static final String DIV_GOLS_EQUIPE_VISITANTE = "div[class=imso_gs__tgs imso_gs__right-team]";
	private static final String ITEM_GOL = "div[class=imso_gs__gs-r]";
	
	
	private static final String DIV_PENALIDADES = "div[class=imso_mh_s__psn-sc]";
	
	private static final String CASA = "casa";
	private static final String VISITANTE = "visitante";
	
	private static final String HTTPS = "https:"; 
	private static final String SRC = "src";
	private static final String  SPAN = "span";

			
	public static void main(String[] args) {
		
		String url = BASE_URL_GOOGLE + "vasco+x+corinthians" + COMPLEMENTO_URL_GOOGLE;
		
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
				
				Integer placarEquipeCasa = recuperaPlacarEquipe(document, DIV_PLACAR_EQUIPE_CASA);
				LOGGER.info("Placar Equipe Casa: {}", placarEquipeCasa);
				
	            Integer placarEquipeVisitante = recuperaPlacarEquipe(document, DIV_PLACAR_EQUIPE_VISITANTE);
	            LOGGER.info("Placar Equipe Visitange: {}", placarEquipeVisitante);

	            String golsEquipeCasa = recuperaGolsEquipe(document, DIV_GOLS_EQUIPE_CASA);
	            System.out.println("Gols Equipe Casa: " + golsEquipeCasa);

	            String golsEquipeVisitante = recuperaGolsEquipe(document, DIV_GOLS_EQUIPE_VISITANTE);
	            System.out.println("Gols Equipe Visitante: " +golsEquipeVisitante);
				
	            Integer placarEstendidoEquipeCasa = buscaPenalidades(document, CASA);
	            LOGGER.info("placar estendido equipe casa: {}", placarEstendidoEquipeCasa);
	            
	            Integer placarEstendidoEquipeVisitante = buscaPenalidades(document, VISITANTE);
	            LOGGER.info("placar estendido equipe visitante: {}", placarEstendidoEquipeVisitante);
				
				
				}
			
			String nomeEquipeCasa = recuperaNomeEquipe(document, DIV_NOME_EQUIPE_CASA);
			LOGGER.info("Nome Equipe Casa: {}", nomeEquipeCasa);
			
			String nomeEquipeVisitante = recuperaNomeEquipe(document, DIV_NOME_EQUIPE_VISITANTE);
			LOGGER.info("Nome Equipe Visitante: {}", nomeEquipeVisitante);
			
			

			
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
		
		boolean isTempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).isEmpty();
		
		if(!isTempoPartida) {
			String tempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			if(tempoPartida.contains("Pênaltis")) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
			
			//LOGGER.info(tempoPartida);
			
		}
		
		isTempoPartida = document.select(DIV_PARTIDA_ENCERRADA).isEmpty();
		if(!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
		
		//LOGGER.info(statusPartida.toString());
		return statusPartida;
		
		
	}
	
	public String obtemTempoPartida(Document document) {
		String tempoPartida = null;
		
		//jogo rolando ou intervalo ou penaltis
		boolean isTempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).isEmpty();
		if(!isTempoPartida) {
			tempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).first().text();
		}
		
		isTempoPartida = document.select(DIV_PARTIDA_ENCERRADA).isEmpty();
		if(!isTempoPartida) {
			tempoPartida = document.select(DIV_PARTIDA_ENCERRADA).first().text();
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
	
	public String recuperaNomeEquipe(Document document, String itemHtml) {
		Element elemento = document.selectFirst(itemHtml);
		String nomeEquipe = elemento.select(SPAN).text();
		return nomeEquipe;
	}
	
//	public String recuperaNomeEquipeVisitante(Document document) {
//		Element elemento = document.selectFirst();
//		String nomeEquipe = elemento.select("span").text();
//		return nomeEquipe;
//	}
	
	   public static Integer recuperaPlacarEquipe(Document document, String itemHtml){

           String placarEquipe = document.selectFirst(itemHtml).text();
           return formatarPlacarStringInteger(placarEquipe);

  }

//   public static Integer recuperaPlacarEquipeVisitante(Document document){
//       String placarEquipe = document.selectFirst().text();
//       return formatarPlacarStringInteger(placarEquipe);
//   }
	
   
   public static String recuperaGolsEquipe(Document document, String itemHtml){
       List<String> golsEquipe = new ArrayList<>();
       Elements elementos = document.select(itemHtml).select(ITEM_GOL);

       for (Element e: elementos) {
           String infoGols = e.select(ITEM_GOL).text();
           golsEquipe.add(infoGols);
       }

       return String.join(", ", golsEquipe);
   }

//   public static String recuperaGolsEquipeVisitante(Document document){
//       List<String> golsEquipe = new ArrayList<>();
//       Elements elemento = document.select(DIV_GOLS_EQUIPE_VISITANTE).select(ITEM_GOL);
//       for (Element e : elemento) {
//           String infGols = e.select(ITEM_GOL).text();
//           golsEquipe.add(infGols);
//       }
//
//       return String.join(", ", golsEquipe);
//
//   }
   
   
   public static Integer buscaPenalidades(Document document, String tipoEquipe) {
	   boolean isPenalidades = document.select(DIV_PENALIDADES).isEmpty();
	   
	   if(!isPenalidades) {
		   String penalidades = document.select(DIV_PENALIDADES).text();
		   String penalidadesCompleta = penalidades.substring(0, 5).replace(" ", "");
		   String[] divisao = penalidadesCompleta.split("-");
		   
		   return tipoEquipe.equals(CASA) ? formatarPlacarStringInteger(divisao[0]) : formatarPlacarStringInteger(divisao[1]);
		   
		  // LOGGER.info("Penalidades: {}", penalidadesCompleta);
	   }
	   
	   
	   return null;
   }
   
   public static Integer formatarPlacarStringInteger(String placar) {
	   Integer valor;
	   try {
		   
		   valor = Integer.parseInt(placar);
	   }catch(Exception e){
		   valor = 0;
	   }
	   
	   return valor;
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
