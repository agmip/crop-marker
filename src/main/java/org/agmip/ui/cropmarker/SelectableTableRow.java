package org.agmip.ui.cropmarker;

/**
 * Basic table row class with check box feature supported.
 *
 * @author Meng Zhang
 */
public class SelectableTableRow {

    private boolean checkFlg = false;

    public boolean getCheckFlg() {
        return this.checkFlg;
    }

    public void setCheckFlg(boolean checkFlg) {
        this.checkFlg = checkFlg;
    }

    @Override
    public String toString() {
        return checkFlg + "";
    }
}
