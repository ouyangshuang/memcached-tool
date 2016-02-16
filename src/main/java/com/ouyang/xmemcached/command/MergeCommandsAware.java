package com.ouyang.xmemcached.command;

import java.util.Map;

/**
 * Merge commands aware interface.Merge commands mean that merge get commands to
 * a bulk-get commands.
 *
 * @author boyan
 */
public interface MergeCommandsAware {

    Map<Object, Command> getMergeCommands();

    void setMergeCommands(Map<Object, Command> mergeCommands);

}
