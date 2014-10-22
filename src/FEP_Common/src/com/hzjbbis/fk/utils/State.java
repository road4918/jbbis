package com.hzjbbis.fk.utils;

public class State {
	public static final State STOPPED = new State("ֹͣ״̬",0);
	public static final State STARTING = new State("��������",1);
	public static final State RUNNING = new State("����״̬",2);
	public static final State STOPPING = new State("����ֹͣ",3);
	public static final State RESTART = new State("�Զ���������",4);

	private String desc;
	private int state;

	public State() {
		this.state = 0;
		this.desc = "ֹͣ״̬";
	}
	private State(String desc,int val) {
		this.state = val;
		this.desc = desc;
	}
	
	public String toString() {
		return desc;
	}
	
	public int getState(){
		return state;
	}
	
	public boolean isStopped(){
		return state == 0;
	}
	public boolean isStarting(){
		return state == 1;
	}
	public boolean isActive(){
		return state == 2;
	}
	public boolean isRunning(){
		return state == 2;
	}
	public boolean isStopping(){
		return state ==3;
	}
	
	public void setStopped(){
		state = 0;
		desc = "ֹͣ״̬";
	}
	public void setStarting(){
		state = 1;
		desc = "��������";
	}
	public void setRunning(){
		state = 2;
		desc = "��������";
	}
	public void setStopping(){
		state = 3;
		desc = "����ֹͣ";
	}
}
