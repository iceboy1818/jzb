package jw.zookeeper.utils;

import java.util.List;

public class JwZooKeeperDataContainer {
	
	public static List<String>  serviceList;

	public static List<String> getServiceList() {
		return serviceList;
	}

	public static void setServiceList(List<String> serviceList) {
		JwZooKeeperDataContainer.serviceList = serviceList;
	}
	
	
}
