/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2025 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.saf;

import java.net.Socket;
import java.io.IOException;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.Log;
import org.jpos.util.NameRegistrar;
import org.jpos.ee.status.Status;
import org.jpos.ee.status.MonitorTask;

public class SAFMonitor extends Log implements MonitorTask, Configurable {
    Configuration cfg;
    String safName;
    public String checkService () {
        boolean rc = false;
        SAF saf;
        try {
            saf = SAF.getSAF (safName);
        } catch (NameRegistrar.NotFoundException e) {
            return Status.ERROR + " saf not found";
        }
        return Status.OK + " " + saf.getStatus();
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        safName = cfg.get ("saf", null);
        if (safName == null)
            throw new ConfigurationException ("property 'saf' is null");
    }
}

