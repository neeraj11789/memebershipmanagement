package com.atlassian.interviews.uag.memory;

import com.atlassian.interviews.uag.api.Group;
import com.atlassian.interviews.uag.api.GroupService;
import com.atlassian.interviews.uag.api.MembershipService;
import com.atlassian.interviews.uag.api.User;
import com.atlassian.interviews.uag.core.AbstractService;
import com.atlassian.interviews.uag.core.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the group service that stores all groups in memory.
 */
@ParametersAreNonnullByDefault
public class MemoryGroupService extends AbstractService implements GroupService {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryGroupService.class);

    private final Map<String, Group> groups = new HashMap<>();

    public MemoryGroupService(Services services) {
        super(services);
    }

    public Group findByName(String name) {
        requireNonNull(name, "name");
        return groups.get(name);
    }

    public void create(Group group) {
        requireNonNull(group, "group");
        if (groups.containsKey(group.getName())) {
            throw new IllegalArgumentException("Group " + group.getName() + " already exists");
        }
        groups.put(group.getName(), group);
        LOG.debug("Created group: {}", group.getName());
    }

    public void delete(Group group) {
        requireNonNull(group, "group");
        /**
         * Before deleting the group we need to ensure all the members
         * are removed from the group
         */
        MembershipService membershipService = services.getMembershipService();
        Collection<User> users = membershipService.getUsersInGroup(group);

        for(User user: users)
            membershipService.removeUserFromGroup(user, group);

        groups.remove(group.getName());
        LOG.debug("Deleted group: {}", group.getName());
    }
}
