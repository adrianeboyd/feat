/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.purl.jh.feat.diffui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.purl.jh.feat.NbData.LLayerDataObject;
import org.purl.jh.feat.diffui.api.Api;
import org.purl.jh.feat.diffui.util.Util;
import org.purl.jh.nbpml.LayerDataObject;
import org.purl.jh.util.Pair;
import org.purl.jh.util.err.XException;

@ActionID(
        category = "File",
        id = "org.purl.jh.feat.diffui.OpenFolderDiffAction"
)
@ActionRegistration(
        iconBase = "org/purl/jh/feat/diffui/folders.png",
        displayName = "#CTL_OpenFolderDiffAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 886),
    @ActionReference(path = "Toolbars/File", position = 385),
    @ActionReference(path = "Shortcuts", name = "D-G")
})
@Messages("CTL_OpenFolderDiffAction=Compare Annotation Folders")
public final class OpenFolderDiffAction implements ActionListener {

    private final static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(OpenFolderDiffAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Pair<LLayerDataObject, LLayerDataObject>> dobjPairs = getPairsOfDObjs();

        System.out.println("DOBJPAIRS: " + dobjPairs);

        if (dobjPairs == null) {
            return;
        }
        // todo check basic consistency
        for (Pair p : dobjPairs) {
            Api.openDiff((LayerDataObject<?>) p.mFirst, (LayerDataObject<?>) p.mSecond);
        }
    }

    private List<Pair<LLayerDataObject, LLayerDataObject>> getPairsOfDObjs() {
        for (;;) {
            final JButton ok = new JButton(bundle.getString("OpenFolderDiffAction.OK"));
            final JButton cancel = new JButton(bundle.getString("OpenFolderDiffAction.Cancel"));
            final OpenFolderPanel p = new OpenFolderPanel(ok);

            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(p, bundle.getString("OpenFolderDiffAction.title"));
            nd.setOptions(new Object[]{ok, cancel});
            Object o = DialogDisplayer.getDefault().notify(nd);

            List<Pair<LLayerDataObject, LLayerDataObject>> pairs = new ArrayList<>();

            if (o != ok) {
                return null;
            }
            try {
                List<File> files1 = Arrays.asList(p.getFile1().listFiles());
                List<File> files2 = Arrays.asList(p.getFile2().listFiles());

                Set<String> llayerFilenames = filterLLayerFiles(files1);
                llayerFilenames.retainAll(filterLLayerFiles(files2));

                for (File f1 : files1) {
                    if (llayerFilenames.contains(f1.getName())) {
                        if (f1.getName().matches(".*a\\.xml$") &&
                                llayerFilenames.contains(f1.getName().replaceAll("a\\.xml$", "b.xml"))) {
                            continue;
                        }

                        System.out.println("Adding: " + f1.getAbsolutePath() + " " + p.getFile2().getPath() + " " + f1.getName());

                        pairs.add(new Pair<>(Util.dobj(f1), Util.dobj(new File(p.getFile2().getPath(), f1.getName()))));
                    }
                }

                // pairs.add(new Pair<>(Util.dobj(f1), Util.dobj(f2)));
                return pairs;
            } catch (XException ex) {
                Util.message(ex, "Cannot open the documents (%s)", ex.getMessage());
            }
        }
    }

    private Set<String> filterLLayerFiles(List<File> files) {
        Set<String> fileNameSet = new HashSet<>();
        for (File f : files) {
            if (f.isFile() && f.getName().matches(".*[ab]\\.xml$")) {
                fileNameSet.add(f.getName());
            }
        }

        return fileNameSet;
    }

}
