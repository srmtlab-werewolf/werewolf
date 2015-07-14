package werewolf;


import org.aiwolf.client.base.player.AbstractSeer;
import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.*;

public class MySeer extends AbstractSeer {
	
	boolean isComingOut = false;
	
	/*
	 * MyThinkJob[プレイヤー番号][職業]
	 * 0:村人 1:占い師 2:霊能者 3:狩人 4:人狼 5:狂人
	 */

	double MyThinkJob[][] = new double[16][5];
	
	int readTalkNum = 0;
	List<Judge> myToldJudgeList = new ArrayList<Judge>();
	List<Agent> TrueJobList = new ArrayList<Agent>();
	MyRoleAssignPlayer mrap;
	
	public MySeer(MyRoleAssignPlayer mrap){
		super();
		this.mrap = mrap;
	}
	
	@Override
	public void dayStart(){
		super.dayStart();
		readTalkNum=0;
	}
	
	@Override
	public void update(GameInfo gameInfo){
		List<Agent> fakeSeerCOAgent = new ArrayList<Agent>();
		
		super.update(gameInfo);
		//今日のログを取得
		List<Talk> talkList = gameInfo.getTalkList();
		for(int i = readTalkNum; i<talkList.size(); i++){
			Talk talk = talkList.get(i);
			//発話をパース
			Utterance utterance = new Utterance(talk.getContent());
			
			//発話のトピックごとに処理
			switch(utterance.getTopic()){
			case COMINGOUT:
				//自分以外で占い師COしているプレイヤーの場合
				if(utterance.getRole() == Role.SEER
				&& !talk.getAgent().equals(getMe())){
					fakeSeerCOAgent.add(utterance.getTarget());
				}
				break;
			case DIVINED:
				//占い結果の発話処理
				break;
			default:
				break;
			}
			readTalkNum++;
		}
	}

	@Override
	public Agent divine() {
		// TODO 自動生成されたメソッド・スタブ
		//占い対象の候補者リスト
		List<Agent> divineCandidates = new ArrayList<Agent>();
		
		//生きているプレイヤーを候補者リストに加える
		divineCandidates.addAll(getLatestDayGameInfo().getAliveAgentList());
		
		//自分自身とすでに占ったことのあるプレイヤーは候補から外す
		divineCandidates.remove(getMe());
		for(Judge judge: getMyJudgeList()){
			if(divineCandidates.contains(judge.getTarget())){
				divineCandidates.remove(judge.getTarget());
			}
		}
		if(divineCandidates.size()>0){
			//候補リストからランダムに選択
			//このとき選択した対象のパラメータ抜き出し(自分から見た職業・真の職業)
			
			int num = new Random().nextInt(divineCandidates.size());
			for(int i=0;i<5;i++)
				System.out.println(MyThinkJob[num][i]);
			TrueJobList.add(divineCandidates.get(num));
			return divineCandidates.get(num);
			//return randomSelect(divineCandidates);
		}else{
			//候補者がいない場合は自分を占い
			return getMe();
		}
	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ
		for(Agent agt : TrueJobList){
			System.out.println(agt);
		}

	}

	@Override
	public String talk() {	
		//占いで人狼を見つけたらCO
		if(!isComingOut){
			for(Judge judge: getMyJudgeList()){
				if(judge.getResult() == Species.WEREWOLF){//占い結果が人狼なら
					String comingoutTalk = TemplateTalkFactory.comingout(getMe(),getMyRole());
					isComingOut = true;
					return comingoutTalk;
				}
			}
		}else{
			for(Judge judge: getMyJudgeList()){
				if(!myToldJudgeList.contains(judge)){//まだ報告していないJudgeの場合
					String resultTalk = TemplateTalkFactory.divined(judge.getTarget(), judge.getResult());
					myToldJudgeList.add(judge);
					return resultTalk;
				}
			}
		}
		//話すことがなければ終了
		return Talk.OVER;
	}

	@Override
	public Agent vote() {
		//投票対象の候補者リスト
		List<Agent> voteCandidates = new ArrayList<Agent>();
		
		List<Agent> whiteAgent = new ArrayList<Agent>(), //白判定だったプレイヤー
					blackAgent = new ArrayList<Agent>(); //黒判定だったプレイヤー
		
		//今まで占ったプレイヤーを白と黒に分ける
		for(Judge judge: getMyJudgeList()){
			
			if(getLatestDayGameInfo().getAliveAgentList().contains(judge.getTarget())){
				switch (judge.getResult()){
				case HUMAN:
					whiteAgent.add(judge.getTarget());
					break;
				case WEREWOLF:
					blackAgent.add(judge.getTarget());
				}
			}
		}
		if(blackAgent.size()>0){
			//黒がいればその中から選択
			return randomSelect(blackAgent);			
		}else{
			//投票対象の候補者リスト
			
			//生きているプレイヤーを候補者リストに加える
			voteCandidates.addAll(getLatestDayGameInfo().getAliveAgentList());
			
			//自分自身と白判定のプレイヤーは候補から外す
			voteCandidates.remove(getMe());
			voteCandidates.removeAll(whiteAgent);
			
			return randomSelect(voteCandidates);
		}
	}
	
	private Agent randomSelect(List<Agent> agentList){
		int num = new Random().nextInt(agentList.size());
		return agentList.get(num);
	}

}
