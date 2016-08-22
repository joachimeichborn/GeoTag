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

package joachimeichborn.geotag.ui.parts;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import net.miginfocom.swt.MigLayout;

public class ProgressView {
	private static class ProgressMonitor extends NullProgressMonitor {
		private final Composite parent;
		Composite entry;
		ProgressBar progressBar;

		public ProgressMonitor(final Composite aParent) {
			parent = aParent;
		}

		@Override
		public void beginTask(final String aName, final int aTotalWork) {
			sync.syncExec(new Runnable() {
				@Override
				public void run() {
					entry = new Composite(parent, SWT.BORDER);
					entry.setLayoutData("growx");
					entry.setLayout(new MigLayout("fill, wrap 1"));
					new Label(entry, SWT.NONE).setText(aName);
					progressBar = new ProgressBar(entry, SWT.SMOOTH | (aTotalWork < 0 ? SWT.INDETERMINATE : SWT.NONE));
					progressBar.setLayoutData("growx");
					if (aTotalWork > 0) {
						progressBar.setMaximum(aTotalWork);
					}
					parent.layout(true, true);
				}
			});
		}

		@Override
		public void worked(final int aWork) {
			sync.syncExec(new Runnable() {
				@Override
				public void run() {
					progressBar.setSelection(progressBar.getSelection() + aWork);
				}
			});
		}

		@Override
		public void done() {
			sync.syncExec(new Runnable() {
				@Override
				public void run() {
					entry.dispose();
					parent.layout(true, true);
				}
			});
		}
	}

	private static UISynchronize sync;

	@Inject
	public ProgressView(final UISynchronize aSync) {
		sync = Objects.requireNonNull(aSync);
	}

	@PostConstruct
	public void createControls(final Composite aParent) {
		aParent.setLayout(new MigLayout("fillx, wrap 1"));
		aParent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		Job.getJobManager().setProgressProvider(new ProgressProvider() {
			@Override
			public IProgressMonitor createMonitor(final Job aJob) {
				return new ProgressMonitor(aParent);
			}
		});
	}
}
