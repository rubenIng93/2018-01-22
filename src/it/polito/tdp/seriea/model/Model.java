package it.polito.tdp.seriea.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {
	
	private Team squadraSelezionata;
	private Map<Season, Integer> punteggi;
	private List<Team> squadre;
	private List<Season> stagioni;
	private SerieADAO dao;
	private Map<Integer, Season> stagioniIdMap;
	private Map<String, Team> squadreIdMap;
	private List<Season> stagioniConsecutive;
	private List<Season> percorsoBest;
	
	private Graph<Season, DefaultWeightedEdge> grafo;
	
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
		
		this.squadraSelezionata = squadra;
		
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
	
	public Season calcolaAnnataDoro() {
		
		//costruisco il grafo
		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(this.grafo, punteggi.keySet());
		
		
		
		for(Season s1 : punteggi.keySet()) {
			for(Season s2 : punteggi.keySet()) {
				if(!s1.equals(s2)) {
					int punti1 = punteggi.get(s1);
					int punti2 = punteggi.get(s2);
					if(punti1 > punti2) {
						Graphs.addEdge(grafo, s2, s1, punti1 - punti2);
					}else{
						Graphs.addEdge(grafo, s1, s2, punti2 - punti1);
					}
				}
			}
		}		
		
		//trovo l'annata migliore
		Season migliore = null;
		int max = 0;
		
		for(Season s : grafo.vertexSet()) {
			
			int valore = pesoStagione(s);
			if(valore > max) {
				max = valore;
				migliore = s;
			}
			
		}
		
		return migliore;
	}
	
	private int pesoStagione(Season s) {
		int somma = 0;
		
		for(DefaultWeightedEdge e : grafo.incomingEdgesOf(s)) {
			somma += (int) grafo.getEdgeWeight(e); 
		}
		
		for(DefaultWeightedEdge e : grafo.outgoingEdgesOf(s)) {
			somma -= (int) grafo.getEdgeWeight(e); 
		}
		
		return somma;
		
		
	}
	
	public List<Season> camminoVirtuoso() {
		
		//trova le stagioni consecutive
		this.stagioniConsecutive = new ArrayList<>(punteggi.keySet());
		Collections.sort(this.stagioniConsecutive);
		
		//prapara le variabili utili alla ricorsione
		List<Season> parziale = new ArrayList<>();
		this.percorsoBest = new ArrayList<>();
		
		//itera al livello 0
		for(Season s : grafo.vertexSet()) {
			parziale.add(s);
			cerca(1, parziale);
			parziale.remove(0);
		}
		
		return percorsoBest;
		
		
		
	}
	
	/*
	 *RICORSIONE
	 *
	 * Soluzione parziale lista di Season
	 * Livello è la lunghezza del percorso (della List)
	 * Casi terminali: non trovo nessun vertice da aggiungere
	 * 	-> verifico se i lcammino ha lunghezza massima tra quelli trovati finora
	 * Generazione delle soluzioni: vertici connessi all'ultimo vertice del percorso
	 * (con arco orientato nel verso giusto), non ancora parte del percorso, 
	 * relativi a stagini consecutive
	 * 
	 */
	
	private void cerca(int livello, List<Season> parziale) {
		boolean trovato = false;
		//genera nuove soluzioni
		Season ultimo = parziale.get(livello -1);
		for(Season prossimo : Graphs.successorListOf(this.grafo, ultimo)) {
			
			if(!parziale.contains(prossimo)) {
				if(stagioniConsecutive.indexOf(ultimo) + 1 == stagioniConsecutive.indexOf(prossimo)) {
					//candidato accettabile -> fai ricorsione
					trovato = true;
					parziale.add(prossimo);
					cerca(livello +1 , parziale);
					parziale.remove(livello);
				}
			}
			
		}
		
		//valuta caso terminale
		if(!trovato) {
			if(parziale.size() > percorsoBest.size()) {
				percorsoBest = new ArrayList<>(parziale);//clona il best
			}
		}
	}
	
	
	

}
