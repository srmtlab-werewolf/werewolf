package werewolf;

import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;

import java.util.*;

public class PlayOut{
	
	MyRoleAssignPlayer mrap;
	myNode mn;
	
	public PlayOut(MyRoleAssignPlayer mrap,myNode mn) {
		super();
		this.mrap = mrap;
		this.mn = mn;
		
	}

	public List<Agent> getAliveAgentList() {
		// TODO 自動生成されたメソッド・スタブ
		List<Agent> temp = new ArrayList<Agent>();
		for(int i =0;i<map.size();i++){
			if(map.get(mrap.AllAgent.get(i)) == Status.ALIVE){
				temp.add(mrap.AllAgent.get(i));
			}
		}
		return temp;
    }
	
	public List<Agent> getDeadAgentList() {
		// TODO 自動生成されたメソッド・スタブ
		List<Agent> temp = new ArrayList<Agent>();
		for(int i =0;i<map.size();i++){
			if(map.get(mrap.AllAgent.get(i)) == Status.DEAD){
				temp.add(mrap.AllAgent.get(i));
			}
		}
		return temp;
    }
	
	public double MyThinkJob[][]= mrap.MyThinkJob;
	public double HaveTrust[][] = mrap.HaveTrust;
	public Map<Agent,Status> map = mrap.map;
	//public List<myNode> tree = new ArrayList<myNode>();
	public Integer wolfnum = mrap.AliveWEREWOLFNum;
	public Integer villagernum = mrap.NowNum-wolfnum;
	public List<Agent> alives = getAliveAgentList();
	public Collection<Agent> deads = getDeadAgentList();
    public List<Agent> villagers = new ArrayList<Agent>();
    public int ran = new Random().nextInt(alives.size());    
    public int votednum[]= new int[16];
    public Agent executed;
    public int VorW;
    public double winrate=0;
    public int playtime=0;
    public boolean udead=false;
    public GameInfo gi = new GameInfo(); 
    int i,j,k;
    double di,dj,dk;

    public double play(List<myNode> tree) {
    	while(true){
		    if(!udead){
		    	votednum[ran]+=1;;
		    }
		    
			for(i=1;i<=15;i++){
				int itemp=0;
				double dtemp=0.0;
				for(j=0;j<6;j++){
					if(MyThinkJob[i][j]>dtemp){
						dtemp=MyThinkJob[i][j];
						itemp = j;
					}
				}
				if(itemp==4)
					villagers.add(mrap.AllAgent.get(i));
			}
		
			for(i=0;i<16;i++){
				votednum[i]=0;
			}
			
			for(i=1;i<=15;i++){
				int itemp = 0;
				double dtemp = 0.0;
				
				for(int j=1;j<=15;j++){
					if(dtemp<HaveTrust[j][i]){
						dtemp=HaveTrust[j][i];
						itemp = j;
					}
				}
				votednum[itemp]+=1;
			}
		
			for(i=1;i<=15;i++){
				int temp=0;
				if(temp<votednum[i]){
					temp=votednum[i];
					j=i;
				}
			}
			executed = alives.get(j);
			deads.add(executed);
		
			if(executed == gi.getAgent())
				udead=true;
			
			for(i=0;i<6;i++){
				int itemp=0;
				double dtemp=0.0;
				if(dtemp<MyThinkJob[j][i]){
					dtemp=MyThinkJob[j][i];
					itemp = i;
				}
				VorW=itemp;
			}
			if(VorW==4)
				wolfnum-=1;
			else
				villagernum-=1;
		
			if(wolfnum<=0){
				winrate = 1.0;
				playtime++;
				return 1.0;
			}else if(wolfnum>=villagernum){
				winrate = 0.0;
				playtime++;
				return 0.0;
		    }
			
			int attacked = new Random().nextInt(villagers.size()); 
			deads.add(villagers.get(attacked));
			if(villagers.get(attacked) == gi.getAgent())
				udead=true;
			villagernum-=1;
			
			if(wolfnum<=0){
				winrate = 1.0;
				playtime++;
				return 1.0;
			}else if(wolfnum>=villagernum){
				winrate = 0.0;
				playtime++;
				return 0.0;
		    }
	
		
	   	}
    }
}
