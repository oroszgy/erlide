package org.erlide.ui.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.erlide.core.erlang.IErlElement;
import org.erlide.ui.ErlideUIPlugin;
import org.erlide.ui.editors.erl.outline.ErlangElementImageProvider;

public class ErlangFileLabelProvider extends LabelProvider implements
        IDescriptionProvider {

    private final NavigatorProblemsDecorator fProblemDecorator;

    public ErlangFileLabelProvider() {
        super();
        fProblemDecorator = new NavigatorProblemsDecorator();
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof IErlElement) {
            final IErlElement e = (IErlElement) element;
            final ImageDescriptor desc = ErlangElementImageProvider
                    .getErlImageDescriptor(e,
                            ErlangElementImageProvider.SMALL_ICONS);
            final Image img = ErlideUIPlugin.getImageDescriptorRegistry().get(
                    desc);
            return fProblemDecorator.decorateImage(img, e);
        }
        return null;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof IErlElement) {
            final IErlElement data = (IErlElement) element;
            return data.toString();// + "= " + data.getValue();
        }
        return null;
    }

    public String getDescription(final Object anElement) {
        if (anElement instanceof IErlElement) {
            final IErlElement data = (IErlElement) anElement;
            return "Property: " + data.toString(); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public void addListener(final ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void removeListener(final ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

}
