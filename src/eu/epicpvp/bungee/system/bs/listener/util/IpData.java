package eu.epicpvp.bungee.system.bs.listener.util;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IpData {

	private final String ip;
	private final Rate differentNameJoinRate = new Rate(2, TimeUnit.HOURS);
	private final Rate loginHubLeaveRate = new Rate(2, TimeUnit.HOURS);
	private final Cache<String, Integer> nameJoinCounter =
			CacheBuilder.newBuilder()
					.expireAfterWrite(2, TimeUnit.HOURS)
					.build();
}
