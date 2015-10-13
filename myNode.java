package werewolf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aiwolf.common.data.Agent;


@SuppressWarnings("serial")
public class myNode implements Serializable {

	public int num;
	//public Double ucb1 = 0.0;
	public int parent;
	public List<Integer> children = new ArrayList<Integer>();
	public double winrate;
	public int playtime;
	public Collection<Agent> deads;
	public int wolfnum;
	public int villagernum;
	
	public myNode() {
	}
	
	public myNode(int num, int parent) {
		this.num = num;
		this.parent = parent;
	}

	public void addparent(int parent) {
		this.parent=parent;
	}
	public void addchild(int child) {
		this.children.add(child);
	}
	public void winrate(int time,double rate){
		winrate*=playtime;
		playtime+=time;
		winrate+=rate;
		winrate/=time;
	}
	public void adddeads(Agent dead){
		deads.add(dead);
	}
	public void addwolfvillagernum(int wolf,int villager){
		wolfnum = wolf;
		villagernum = villager;
	}
	
	//public void calcUCB1(double xi, int ni, int n){
	//	ucb1 = xi + 2*Math.sqrt(2*Math.log(n)/ni);
	//}

	//@Override
	//public String toString() {
	//	return String.format("%s->%s", name, children);
	//}
}