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

package org.jpos.qi.system;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.q2.CLI;
import org.jpos.q2.QFactory;
import org.jpos.qi.QI;
import org.jpos.server.ui.XLabel;
import org.jpos.util.Loggeable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jpos.qi.QI.getQI;

public class CommandRunner extends CssLayout implements View, XmlConfigurable, Runnable, ViewChangeListener {
    private QFactory qfactory = getQI().getQ2().getFactory();
    private String command;
    private Thread runner;
    private AtomicBoolean active = new AtomicBoolean();
    protected QI qi;
    protected Label label;
    protected long repeat;

    public CommandRunner () {
        qi = QI.getQI();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        if (needsThread()) {
            active.set(true);
            runner = new Thread(this, getClass().getName());
            event.getNavigator().addViewChangeListener(this);
            runner.start();
        } else {
            try {
                label.setValue(CLI.exec(command));
            } catch (Exception e) {
                qi.getLog().warn (e);
            }
        }
    }

    @Override
    public void run() {
        while (active.get()) {
            try {
                label.setValue(CLI.exec(command));
                qi.access(() -> qi.push());
                Thread.sleep (repeat);
            } catch (UIDetachedException e) {
                break;
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                qi.getLog().warn (e);
            }
        }
    }
    @Override
    public boolean beforeViewChange(ViewChangeListener.ViewChangeEvent event) {
        if (event.getNewView() != this) {
            active.set(false);
            runner.interrupt();
            try {
                runner.join();
            } catch (InterruptedException ignore) { }
            removeAllComponents();
        }
        return true;
    }

    @Override
    public void afterViewChange(ViewChangeListener.ViewChangeEvent event) { }

    @Override
    public void setConfiguration(Element e) throws ConfigurationException {
        Element ce = e.getChild("command");
        if (ce != null)
            command = ce.getTextTrim();

        if (command != null) {
            String viewStyle = e.getAttributeValue("viewStyle");
            String objectStyle = e.getAttributeValue("commandStyle");
            boolean clearScreen = "true".equalsIgnoreCase(e.getAttributeValue("clearScreen"));
            repeat = getRepeat (e);
            label = clearScreen ? new Label() : new XLabel();
            label.setContentMode(ContentMode.PREFORMATTED);
            if (viewStyle != null)
                addStyleName (viewStyle);
            if (objectStyle != null)
                label.addStyleName (objectStyle);
            addComponent(label);
        }
    }

    private long getRepeat (Element e) {
        String s = e.getAttributeValue ("repeat");
        return s != null ? Math.max(Long.parseLong(s), 500L) : 0L; // we don't want to hog the system
    }

    private String toString(Loggeable obj, String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (baos);
        obj.dump (p, indent);
        return baos.toString();
    }

    protected boolean isActive() {
        return active.get();
    }

    protected boolean needsThread() {
        return repeat > 0L;
    }
}
