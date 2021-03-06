package eu.epicpvp.bungee.system.bs.listener.util;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class IpData {

	private final String ip;
	private final Rate differentNameJoinRate = new Rate(2, TimeUnit.HOURS);
	@Setter
	private long lastLoginHubLeave;
	private final Cache<String, Integer> nameJoinCounter =
			CacheBuilder.newBuilder()
					.expireAfterWrite(2, TimeUnit.HOURS)
					.build();
}
