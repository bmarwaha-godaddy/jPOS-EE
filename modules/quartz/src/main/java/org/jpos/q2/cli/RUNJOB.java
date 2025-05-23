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

package org.jpos.q2.cli;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.QuartzAdaptor;
import org.jpos.util.NameRegistrar;
import org.quartz.JobDetail;

public class RUNJOB implements CLICommand {
    @Override
    public void exec(CLIContext cli, String[] args) throws Exception {
        if (args.length != 3) {
            cli.println ("Usage: RUNJOB <cronname> <jobid>");
            return;
        }
        try {
            QuartzAdaptor adaptor = NameRegistrar.get(args[1]);
            JobDetail job = adaptor.getJob(args[2]);
            if (job != null) {
                adaptor.scheduler().triggerJob(job.getKey());
            }
        } catch (NameRegistrar.NotFoundException e) {
            cli.println("QBean '" + args[1] + "' not found");
        }
    }
}
