package org.aiwolf.myAgent;




import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.*;

import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.net.GameSetting;
//import org.aiwolf.player.BasePlayer;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.GameData;
import org.aiwolf.server.net.GameServer;
import org.aiwolf.server.net.DirectConnectServer;

/**
 * �e�X�g�p�ɐl�T���X�^�[�g�����邽�߂�Main�N���X
 * 
 * @author naruse
 *
 */
public class AIWolfroop {


	/**
	 * ���̃G�[�W�F���g�̐�
	 */
	static protected int PLAYER_NUM = 15;
	
	/**
	 * 1��̎��s�ōs���Q�[����
	 * 
	 */
	static protected int GAME_NUM = 100;
	
public static void main(String[]args)throws InstantiationException,IllegalAccessException,ClassNotFoundException,IOException{
	//���l��������
	int villagerWinNum = 0;
	//�l�T��������
	int werewolfWinNum = 0;
	
	for(int i = 0; i < GAME_NUM;i++){
		List<Player> playerList = new ArrayList<Player>();
		
		for(int j = 0; j < PLAYER_NUM; j++){
		   playerList.add(new MyRoleAssignPlayer());//�����ō쐬�����G�[�W�F���g���w��
		}
		GameServer gameServer = new DirectConnectServer(playerList);
		GameSetting gameSetting = GameSetting.getDefaultGame(PLAYER_NUM);
		
		AIWolfGame game = new AIWolfGame(gameSetting, gameServer);
		game.setRand(new Random(gameSetting.getRandomSeed()));
game.start();
if(game.getWinner() == Team.VILLAGER){
	villagerWinNum++;
}else{
	werewolfWinNum++;
}
System.out.println("村人勝利数:"+ villagerWinNum + "人狼勝利数"+ werewolfWinNum);
	}
	
}




}






