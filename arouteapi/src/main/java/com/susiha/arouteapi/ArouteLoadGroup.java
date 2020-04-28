package com.susiha.arouteapi;

import java.util.Map;

public interface ArouteLoadGroup {

    Map<String,Class<? extends ArouteLoadPath>> loadGroup();
}
