/*
GeoTag

Copyright (C) 2015  Joachim von Eichborn

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package joachimeichborn.geotag.handlers;

import java.util.Dictionary;
import java.util.Enumeration;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import net.miginfocom.swt.MigLayout;

public class ShowAboutHandler {
	private static class AboutDialog extends TrayDialog {

		public AboutDialog(final Shell aShell) {
			super(aShell);

		}

		protected void configureShell(final Shell aShell) {
			super.configureShell(aShell);
			aShell.setText("About GeoTag");
		}

		protected Button createButton(final Composite aParent, final int aId, final String aLabel,
				final boolean aDefaultButton) {
			if (aId == IDialogConstants.CANCEL_ID)
				return null;
			return super.createButton(aParent, aId, aLabel, aDefaultButton);
		}

		@Override
		protected Control createDialogArea(final Composite aParent) {
			final Composite composite = new Composite(aParent, SWT.NONE);
			final GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 20;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			applyDialogFont(composite);

			final Label imageLabel = new Label(composite, SWT.NONE);
			imageLabel.setImage(new Image(Display.getCurrent(),
					ShowAboutHandler.class.getResourceAsStream("/icons/about_image.png")));

			final Composite textPane = new Composite(composite, SWT.NONE);
			textPane.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
			textPane.setLayout(new MigLayout("wrap 1"));
			final Label headlineLabel = new Label(textPane, SWT.NONE);
			headlineLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			headlineLabel.setText("GeoTag");
			headlineLabel.setLayoutData("gaptop 20, gapbottom 30, gapright 300");

			new Label(textPane, SWT.NONE).setText("Version " + getVersion());
			new Label(textPane, SWT.NONE).setText("(c) 2015 Joachim von Eichborn");
			new Label(textPane, SWT.NONE).setText(
					"This program is free software: you can redistribute it and/or modify\nit under the terms of the GNU General Public License as published by\nthe Free Software Foundation, either version 3 of the License, or\n(at your option) any later version.");
			new Label(textPane, SWT.NONE).setText(
					"This program is distributed in the hope that it will be useful,\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\nGNU General Public License for more details.");
			new Label(textPane, SWT.NONE).setText(
					"You should have received a copy of the GNU General Public License\nalong with this program.  If not, see <http://www.gnu.org/licenses/>.");
			return composite;
		}

		private String getVersion() {
			final Bundle bundle = FrameworkUtil.getBundle(this.getClass());
			final Dictionary<String, String> headers = bundle.getHeaders();
			final Enumeration<String> keys = headers.keys();
			while (keys.hasMoreElements()) {
				final String key = keys.nextElement();
				if ("Bundle-Version".equals(key)) {
					return headers.get(key);
				}
			}

			return "0.0.0";
		}

	}

	@Execute
	public static void execute(final Shell aShell) {
		new AboutDialog(aShell).open();
	}
}
