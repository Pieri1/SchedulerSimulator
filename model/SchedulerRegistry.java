package model;

import java.util.*;

/**
 * Utility to discover available Scheduler implementations via ServiceLoader
 * and provide names for UI/config without duplication.
 */
public final class SchedulerRegistry {
    private SchedulerRegistry() {}

    /**
     * Returns a map from display name (uppercase) to scheduler class.
     * The display name prefers Scheduler.getName(), falling back to simple class name.
     */
    public static Map<String, Class<? extends Scheduler>> discoverSchedulers() {
        ServiceLoader<Scheduler> loader = ServiceLoader.load(Scheduler.class);
        Map<String, Class<? extends Scheduler>> map = new LinkedHashMap<>();
        for (Scheduler s : loader) {
            String name = s.getName();
            if (name == null || name.isEmpty()) {
                name = s.getClass().getSimpleName();
            }
            map.put(name.toUpperCase(Locale.ROOT), s.getClass());
        }
        return map;
    }

    /**
     * Returns the list of display names for UI (original case preferred: getName or simple name).
     */
    public static java.util.List<String> getSchedulerNames() {
        ServiceLoader<Scheduler> loader = ServiceLoader.load(Scheduler.class);
        java.util.List<String> names = new ArrayList<>();
        for (Scheduler s : loader) {
            String name = s.getName();
            if (name == null || name.isEmpty()) {
                name = s.getClass().getSimpleName();
            }
            names.add(name);
        }
        // Keep FIFO first if present for usability
        java.util.Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        int fifoIdx = -1;
        for (int i = 0; i < names.size(); i++) {
            if ("FIFO".equalsIgnoreCase(names.get(i))) { fifoIdx = i; break; }
        }
        if (fifoIdx > 0) {
            String fifo = names.remove(fifoIdx);
            names.add(0, fifo);
        }
        return names;
    }
}
