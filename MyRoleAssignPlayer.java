package org.aiwolf.myAgent;

import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;
import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.client.base.player.AbstractSeer;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.data.Agent;
public class MyRoleAssignPlayer extends AbstractRoleAssignPlayer {

	//public double[][] myThinkJob;

	MySeer me = new MySeer(this);
	
	public MyRoleAssignPlayer(){
		
		setSeerPlayer(me);
		//setVillagerPlayer(new MyVillager());
		
	}
	
	
	
	
	
	@Override
	public String getName() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return "NIT Shiramatsu lab. "+ "Player";//this.getClass().getName();
	} 
	
	

	public void first(){
		
		
		
		
	}
	
	
}
