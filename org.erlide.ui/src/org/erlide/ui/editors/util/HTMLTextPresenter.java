/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.ui.editors.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.erlide.ui.ErlideUIPlugin;

public class HTMLTextPresenter implements
		DefaultInformationControl.IInformationPresenter,
		DefaultInformationControl.IInformationPresenterExtension {

	private static final String LINE_DELIM = System.getProperty(
			"line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	private int fCounter;

	private final boolean fEnforceUpperLineLimit;

	public HTMLTextPresenter(boolean enforceUpperLineLimit) {
		super();
		fEnforceUpperLineLimit = enforceUpperLineLimit;
	}

	public HTMLTextPresenter() {
		this(true);
	}

	protected Reader createReader(String hoverInfo,
			TextPresentation presentation) {
		return new HTML2TextReader(new StringReader(hoverInfo), presentation);
	}

	protected void adaptTextPresentation(TextPresentation presentation,
			int offset, int insertLength) {

		final int yoursStart = offset;
		int yoursEnd = offset + insertLength - 1;
		yoursEnd = Math.max(yoursStart, yoursEnd);

		final Iterator<?> e = presentation.getAllStyleRangeIterator();
		while (e.hasNext()) {

			final StyleRange range = (StyleRange) e.next();

			final int myStart = range.start;
			int myEnd = range.start + range.length - 1;
			myEnd = Math.max(myStart, myEnd);

			if (myEnd < yoursStart) {
				continue;
			}

			if (myStart < yoursStart) {
				range.length += insertLength;
			} else {
				range.start += insertLength;
			}
		}
	}

	private void append(StringBuilder buffer, String string,
			TextPresentation presentation) {

		final int length = string.length();
		buffer.append(string);

		if (presentation != null) {
			adaptTextPresentation(presentation, fCounter, length);
		}

		fCounter += length;
	}

	private String getIndent(String line) {
		final int length = line.length();

		int i = 0;
		while (i < length && Character.isWhitespace(line.charAt(i))) {
			++i;
		}

		return (i == length ? line : line.substring(0, i)) + " "; //$NON-NLS-1$
	}

	/*
	 * @see IHoverInformationPresenter#updatePresentation(Display display,
	 *      String, TextPresentation, int, int)
	 */
	public String updatePresentation(Drawable drawable, String hoverInfo,
			TextPresentation presentation, int maxWidth, int maxHeight) {

		if (hoverInfo == null) {
			return null;
		}

		final GC gc = new GC(drawable);
		try {

			final StringBuilder buffer = new StringBuilder();
			long maxNumberOfLines = Math.round(maxHeight * 1.0
					/ gc.getFontMetrics().getHeight());

			fCounter = 0;
			final LineBreakingReader reader = new LineBreakingReader(
					createReader(hoverInfo, presentation), gc, maxWidth);

			boolean lastLineFormatted = false;
			String lastLineIndent = null;

			String line = reader.readLine();
			boolean lineFormatted = reader.isFormattedLine();
			boolean firstLineProcessed = false;

			while (line != null) {

				if (fEnforceUpperLineLimit && maxNumberOfLines <= 0) {
					break;
				}

				if (firstLineProcessed) {
					if (!lastLineFormatted) {
						append(buffer, LINE_DELIM, null);
					} else {
						append(buffer, LINE_DELIM, presentation);
						if (lastLineIndent != null) {
							append(buffer, lastLineIndent, presentation);
						}
					}
				}

				append(buffer, line, null);
				firstLineProcessed = true;

				lastLineFormatted = lineFormatted;
				if (!lineFormatted) {
					lastLineIndent = null;
				} else if (lastLineIndent == null) {
					lastLineIndent = getIndent(line);
				}

				line = reader.readLine();
				lineFormatted = reader.isFormattedLine();

				maxNumberOfLines--;
			}

			if (line != null && buffer.length() > 0) {
				append(buffer, LINE_DELIM, lineFormatted ? presentation : null);
				append(buffer, "...", presentation);
			}

			return trim(buffer, presentation);

		} catch (final IOException e) {

			ErlideUIPlugin.log(e);
			return null;

		} finally {
			gc.dispose();
		}
	}

	private String trim(StringBuilder buffer, TextPresentation presentation) {

		final int length = buffer.length();

		int end = length - 1;
		while (end >= 0 && Character.isWhitespace(buffer.charAt(end))) {
			--end;
		}

		if (end == -1) {
			return ""; //$NON-NLS-1$
		}

		if (end < length - 1) {
			buffer.delete(end + 1, length);
		} else {
			end = length;
		}

		int start = 0;
		while (start < end && Character.isWhitespace(buffer.charAt(start))) {
			++start;
		}

		buffer.delete(0, start);
		presentation.setResultWindow(new Region(start, buffer.length()));
		return buffer.toString();
	}

	@Deprecated
	public String updatePresentation(Display display, String hoverInfo,
			TextPresentation presentation, int maxWidth, int maxHeight) {
		return null;
	}
}
