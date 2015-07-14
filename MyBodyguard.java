package werewolf;

import org.aiwolf.client.base.player.AbstractBodyguard;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.kajiClient.lib.AdvanceGameInfo;

import java.util.*;


public class MyBodyguard extends AbstractBodyguard {
	
	/*
	 * MyThinkJob[プレイヤー番号][職業]
	 * 0:村人 1:占い師 2:霊能者 3:狩人 4:人狼 5:狂人
	 */
	double MyThinkJob[][] = new double[16][5];
	List<Agent> divinedMeWolf = new ArrayList<Agent>();
	List<Agent> enemyList = new ArrayList<Agent>();
	List<Agent> allAgents = new ArrayList<Agent>();
	List<Agent> deadAgents = new ArrayList<Agent>();
	List<Agent> importantAgents = new ArrayList<Agent>();
	int readTalkNum = 0;
	
	MyRoleAssignPlayer mrap;
	
	public MyBodyguard(MyRoleAssignPlayer mrap)
	    {
			super();
	        agi = new AdvanceGameInfo();
	        this.mrap = mrap;
	        //allAgents.addAll(getLatestDayGameInfo().getAgentList());
	    
	    }

	@Override
	public void dayStart() {
		// TODO 自動生成されたメソッド・スタブ

	}
	
	@Override
	public void update(GameInfo gameInfo){
		
		super.update(gameInfo);
		//今日のログを取得
		List<Talk> talkList = gameInfo.getTalkList();
		for(int i = readTalkNum; i<talkList.size(); i++){
			Talk talk = talkList.get(i);
			//発話をパース
			Utterance utterance = new Utterance(talk.getContent());
			
			//発話のトピックごとに処理
			switch(utterance.getTopic()){

			case DIVINED:
				//占い結果の発話処理
				if(utterance.getResult() == Species.WEREWOLF
				&& utterance.getTarget().equals(getMe()))
					enemyList.add(talk.getAgent()); 
				break;
			case COMINGOUT:
				//占い師COしているプレイヤーの場合
				if(utterance.getRole() == Role.SEER||utterance.getRole() == Role.MEDIUM
				){
					importantAgents.add(utterance.getTarget());
				}
				break;
			default:
				break;
			}
			readTalkNum++;
		}
	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public Agent guard() {
		//防衛候補の格納用
		List<Agent> guardCandidates = new ArrayList<Agent>();
		allAgents.addAll(getLatestDayGameInfo().getAgentList());
		deadAgents.addAll(getLatestDayGameInfo().getAgentList());
		
		for(Agent agent : getLatestDayGameInfo().getAliveAgentList())
			deadAgents.remove(agent);
		
		for(Agent agent:deadAgents)
			importantAgents.remove(agent);
		

			for(int i=1;i<16;i++){
				if(MyThinkJob[i][4]>0.8){
					importantAgents.remove(allAgents.get(i));
					//enemyList.add(Agent.getAgent(i));
				}
			}
					
			//生存プレイヤーを追加し自分は除外
			guardCandidates.addAll(getLatestDayGameInfo().getAliveAgentList());
			guardCandidates.remove(getMe());
			
			//確定人狼(黒80%以上)も除外(何％以上で除外するかは要検証・モンテカルロ使うべし)
			for(int i=1;i<16;i++){
				if(MyThinkJob[i][4]>0.8){
					guardCandidates.remove(allAgents.get(i));
					importantAgents.remove(allAgents.get(i));
					//enemyList.add(Agent.getAgent(i));
				}
			}
			//自身に占いで黒を出した奴も除外
			for(int i=0;i<enemyList.size();i++){
				importantAgents.remove(enemyList.get(i));
				guardCandidates.remove(enemyList.get(i));
			}
			
			
			if(importantAgents.size()>0){
				int num = new Random().nextInt(importantAgents.size());
				for(int i=0;i<5;i++)
					System.out.println(MyThinkJob[num][i]);
				return importantAgents.get(num);
			}else{
				//残りからランダムで決定
				int num = new Random().nextInt(guardCandidates.size());
				for(int i=0;i<5;i++)
					System.out.println(MyThinkJob[num][i]);
				return guardCandidates.get(num);
			}
		}

	@Override
	public String talk() {
		// TODO 自動生成されたメソッド・スタブ
		return Talk.OVER;
	}

	@Override
	public Agent vote() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	 AdvanceGameInfo agi;
	
}
