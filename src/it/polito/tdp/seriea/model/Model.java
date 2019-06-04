package it.polito.tdp.seriea.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {
	
	private Team squadraSelezionata;
	private Map<Season, Integer> punteggi;
	private List<Team> squadre;
	private List<Season> stagioni;
	private SerieADAO dao;
	private Map<Integer, Season> stagioniIdMap;
	private Map<String, Team> squadreIdMap;
	
	public Model() {
		this.dao = new SerieADAO();
		
		this.squadre = dao.listTeams();
		this.stagioni = dao.listAllSeasons();
		
		this.stagioniIdMap = new HashMap<>();
		for(Season s : this.stagioni) {
			this.stagioniIdMap.put(s.getSeason(), s);
		}
		
		this.squadreIdMap = new HashMap<>();
		for(Team t : this.squadre) {
			this.squadreIdMap.put(t.getTeam(), t);
		}
	}
	
	public List<Team> getSquadre(){
		return this.squadre;
	}
	
	public Map<Season, Integer> calcolaPunteggi(Team squadra) {
		this.punteggi = new HashMap<>();
		
		this.dao = new SerieADAO();
		List<Match> partite = dao.listMathesForTeam(squadra, stagioniIdMap, squadreIdMap);
		
		for(Match m : partite) {
			
			Season stagione = m.getSeason();
			int punti = 0;
			if(m.getFtr().equals("D")) {
				punti = 1;
			}else {
				if((m.getHomeTeam().equals(squadra) && m.getFtr().equals("H")) ||
						(m.getAwayTeam().equals(squadra) && m.getFtr().equals("A") )) {
					punti = 3;
				}
			}
			
			Integer attuale = punteggi.get(stagione);
			if(attuale == null)
				attuale = 0;
			punteggi.put(stagione, attuale + punti);
			
		}
		
		return this.punteggi;
	}
	
	
	

}
