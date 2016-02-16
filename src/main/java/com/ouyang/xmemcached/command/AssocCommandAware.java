package com.ouyang.xmemcached.command;

import java.util.List;

/**
 * Assoc commands aware interface.Association commands mean that commands has
 * the same key.
 *
 * @author dennis
 */
public interface AssocCommandAware {
    List<Command> getAssocCommands();

    void setAssocCommands(List<Command> assocCommands);
}
