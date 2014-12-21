/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathek.tool;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import mediathek.MediathekGui;
import mediathek.controller.Log;
import mediathek.daten.Daten;

public class GuiFunktionen extends Funktionen {

    public static void updateGui(MediathekGui mediathekGui) {
        try {
            SwingUtilities.updateComponentTreeUI(mediathekGui);
            for (Frame f : Frame.getFrames()) {
                SwingUtilities.updateComponentTreeUI(f);
                for (Window w : f.getOwnedWindows()) {
                    SwingUtilities.updateComponentTreeUI(w);
                }
            }
        } catch (Exception ignored) {
        }

    }

    public static void copyToClipboard(String s) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
    }

    public static void getSize(String nr, JFrame jFrame) {
        Daten.mVConfig.add(nr, jFrame.getSize().width + ":"
                + jFrame.getSize().height + ":"
                + jFrame.getLocation().x + ":"
                + jFrame.getLocation().y);
    }

    public static void getSize(String nr, JDialog jDialog) {
        Daten.mVConfig.add(nr, jDialog.getSize().width + ":"
                + jDialog.getSize().height + ":"
                + jDialog.getLocation().x + ":"
                + jDialog.getLocation().y);
    }

    public static void setSize(String nr, JFrame jFrame, JFrame relativFrame) {
        int breite, hoehe, posX, posY;
        breite = 0;
        hoehe = 0;
        posX = 0;
        posY = 0;
        String[] arr = Daten.mVConfig.get(nr).split(":");
        try {
            if (arr.length == 4) {
                breite = Integer.parseInt(arr[0]);
                hoehe = Integer.parseInt(arr[1]);
                posX = Integer.parseInt(arr[2]);
                posY = Integer.parseInt(arr[3]);
            }
        } catch (Exception ex) {
            breite = 0;
            hoehe = 0;
            posX = 0;
            posY = 0;
        }
        if (breite > 0 && hoehe > 0) {
            jFrame.setSize(new Dimension(breite, hoehe));
        }
        if (posX > 0 && posY > 0) {
            jFrame.setLocation(posX, posY);
        } else if (relativFrame != null) {
            jFrame.setLocationRelativeTo(relativFrame);
        }
    }

    public static boolean setSize(String nr, JDialog jDialog, JFrame relativFrame) {
        boolean ret = false;
        int breite, hoehe, posX, posY;
        breite = 0;
        hoehe = 0;
        posX = 0;
        posY = 0;
        String[] arr = Daten.mVConfig.get(nr).split(":");
        try {
            if (arr.length == 4) {
                breite = Integer.parseInt(arr[0]);
                hoehe = Integer.parseInt(arr[1]);
                posX = Integer.parseInt(arr[2]);
                posY = Integer.parseInt(arr[3]);
            }
        } catch (Exception ex) {
            breite = 0;
            hoehe = 0;
            posX = 0;
            posY = 0;
        }
        if (breite > 0 && hoehe > 0) {
            jDialog.setSize(new Dimension(breite, hoehe));
            ret = true;
        }
        if (posX > 0 && posY > 0) {
            jDialog.setLocation(posX, posY);
        } else if (relativFrame != null) {
            jDialog.setLocationRelativeTo(relativFrame);
        }
        return ret;
    }

    public static String checkDateiname(String name, boolean pfad) {
        // dient nur zum Anzeigen falls es Probleme mit dem
        // Namen geben könnte
        // < > ? " : | \ / *
        boolean winPfad = false;
        String ret = name;
        final OperatingSystemType os = getOs();
        if (os == OperatingSystemType.WIN32 || os == OperatingSystemType.WIN64) {
            // win verträgt keine Pfadnamen/Dateinamen mit einem "." am Schluß
            while (!ret.isEmpty() && ret.endsWith(".")) {
                ret = ret.substring(0, ret.length() - 1);
            }
        }
        if (pfad) {
            if (File.separator.equals("/")) {
                ret = ret.replace("\\", "-");
            } else {
                ret = ret.replace("/", "-");
            }
            if (Funktionen.getOs() == OperatingSystemType.WIN32 || Funktionen.getOs() == OperatingSystemType.WIN64) {
                if (ret.length() > 1) {
                    if (ret.charAt(1) == ':') {
                        // damit auch "d:" und nicht nur "d:\" als Pfad geht
                        winPfad = true;
                        ret = ret.replaceFirst(":", ""); // muss zum Schluss wieder rein, kann aber so nicht ersetzt werden
                    }
                }
            }
        } else {
            ret = ret.replace("\\", "-");
            ret = ret.replace("/", "-");
        }
        ret = ret.replace(" ", "_");
        ret = ret.replace("\n", "_");
        ret = ret.replace("\"", "_");
        ret = ret.replace("*", "_");
        ret = ret.replace("?", "_");
        ret = ret.replace("<", "_");
        ret = ret.replace(">", "_");
        ret = ret.replace(":", "_");
        ret = ret.replace("'", "_");
        ret = ret.replace("|", "_");
        if (winPfad) {
            // c: wieder herstellen
            if (ret.length() == 1) {
                ret = ret + ":";
            } else if (ret.length() > 1) {
                ret = ret.charAt(0) + ":" + ret.substring(1);
            }
        }
        return ret;
    }

    public static String replaceLeerDateiname(String name) {
        // aus einem Dateinamen werden verbotene Zeichen entfernt
        // < > ? " : | \ / *
        if (Daten.mVConfig.get(MVConfig.SYSTEM_ZIELNAMEN_ANPASSEN).equals(Konstanten.ZIELNAMEN_ANPASSEN_NIX)) {
            // dann wars das!
            return name;
        }
        String ret = name;

        //  nur für Windows
        final OperatingSystemType os = getOs();
        if (os == OperatingSystemType.WIN32 || os == OperatingSystemType.WIN64) {
            // win verträgt keine Pfadnamen/Dateinamen mit einem "." am Schluß
            while (!ret.isEmpty() && ret.endsWith(".")) {
                ret = ret.substring(0, ret.length() - 1);
            }
        }

        // wir immer entfernt
        if (File.separator.equals("\\")) {
            ret = ret.replace("\\", "");
        } else {
            ret = ret.replace("/", "");
        }
        
        // temp. until change of function
        ret = ret.replace("\n", "");
        ret = ret.replace("\"", "");
        ret = ret.replace("*", "");
        ret = ret.replace("?", "");
        ret = ret.replace("<", "");
        ret = ret.replace(">", "");
        ret = ret.replace(":", "");
        ret = ret.replace("'", "");
        ret = ret.replace("|", "");

        // und jetzt noch Ersetzungstabelle
        if (Boolean.parseBoolean(Daten.mVConfig.get(MVConfig.SYSTEM_USE_REPLACETABLE))) {
            ret = Daten.mVReplaceList.replace(ret, false /*path*/);
        }

        return ret;
    }

    public static String addsPfad(String pfad1, String pfad2) {
        String ret = concatPaths(pfad1, pfad2);
        if (ret.equals("")) {
            Log.fehlerMeldung(283946015, "GuiFunktionen.addsPfad", pfad1 + " - " + pfad2);
        }
        return ret;
    }

    public static String concatPaths(String pfad1, String pfad2) {
        String ret = "";
        if (pfad1 != null && pfad2 != null) {
            if (!pfad1.equals("") && !pfad2.equals("")) {
                if (pfad1.endsWith(File.separator)) {
                    ret = pfad1.substring(0, pfad1.length() - 1);
                } else {
                    ret = pfad1;
                }
                if (pfad2.charAt(0) == File.separatorChar) {
                    ret += pfad2;
                } else {
                    ret += File.separator + pfad2;
                }
            }
        }
        return ret;
    }

    /*
     public static String addUrl(String u1, String u2) {
     if (u1.endsWith("/")) {
     return u1 + u2;
     } else {
     return u1 + "/" + u2;
     }
     }
     */
    public static boolean istUrl(String dateiUrl) {
        //return dateiUrl.startsWith("http") ? true : false || dateiUrl.startsWith("www") ? true : false;
        return dateiUrl.startsWith("http") || dateiUrl.startsWith("www");
    }

    public static String getDateiName(String pfad) {
        //Dateinamen einer URL extrahieren
        String ret = "";
        if (pfad != null) {
            if (!pfad.equals("")) {
                ret = pfad.substring(pfad.lastIndexOf('/') + 1);
            }
        }
        if (ret.contains("?")) {
            ret = ret.substring(0, ret.indexOf('?'));
        }
        if (ret.contains("&")) {
            ret = ret.substring(0, ret.indexOf('&'));
        }
        if (ret.equals("")) {
            Log.fehlerMeldung(395019631, "GuiFunktionen.getDateiName", pfad);
        }
        return ret;
    }

    public static String getDateiSuffix(String pfad) {
        // Suffix einer URL extrahieren
        // "http://ios-ondemand.swr.de/i/swr-fernsehen/bw-extra/20130202/601676.,m,s,l,.mp4.csmil/index_2_av.m3u8?e=b471643725c47acd"
        String ret = "";
        if (pfad != null) {
            if (!pfad.equals("") && pfad.contains(".")) {
                ret = pfad.substring(pfad.lastIndexOf('.') + 1);
            }
        }
        if (ret.equals("")) {
            Log.fehlerMeldung(969871236, "GuiFunktionen.getDateiSuffix", pfad);
        }
        if (ret.contains("?")) {
            ret = ret.substring(0, ret.indexOf('?'));
        }
        if (ret.length() > 5) {
            // dann ist was faul
            ret = "---";
            Log.fehlerMeldung(821397046, "GuiFunktionen.getDateiSuffix", pfad);
        }
        return ret;
    }

    /*
     public static void listeSort(LinkedList<String> liste) {
     //Stringliste alphabetisch sortieren
     GermanStringSorter sorter = GermanStringSorter.getInstance();
     Collections.sort(liste, sorter);
     }
     */
    public static String getHomePath() {
        //lifert den Pfad zum Homeverzeichnis
        return System.getProperty("user.home");
    }

    public static String getStandardDownloadPath() {
        //lifert den Standardpfad für Downloads
        if (getOs() == OperatingSystemType.MAC) {
            return addsPfad(getHomePath(), "Desktop");
        }
        return addsPfad(getHomePath(), Konstanten.VERZEICHNIS_DOWNLOADS);
    }

    public static String[] addLeerListe(String[] str) {
        //ein Leerzeichen der Liste voranstellen
        int len = str.length + 1;
        String[] liste = new String[len];
        liste[0] = "";
        System.arraycopy(str, 0, liste, 1, len - 1);
        return liste;
    }

    public static String textLaenge(int max, String text, boolean mitte, boolean addVorne) {
        if (text.length() > max) {
            if (mitte) {
                text = text.substring(0, 25) + " .... " + text.substring(text.length() - (max - 31));
            } else {
                text = text.substring(0, max - 1);
            }
        }
        while (text.length() < max) {
            if (addVorne) {
                text = " " + text;
            } else {
                text = text + " ";
            }
        }
        return text;
    }

    public static int getImportArtFilme() {
        int ret;
        try {
            ret = Integer.parseInt(Daten.mVConfig.get(MVConfig.SYSTEM_IMPORT_ART_FILME));
        } catch (Exception ex) {
            Daten.mVConfig.add(MVConfig.SYSTEM_IMPORT_ART_FILME, String.valueOf(Konstanten.UPDATE_FILME_AUTO));
            ret = Konstanten.UPDATE_FILME_AUTO;
        }
        return ret;
    }
}
