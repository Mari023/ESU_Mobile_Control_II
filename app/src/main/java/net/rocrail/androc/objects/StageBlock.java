/*
 Rocrail - Model Railroad Software

 Copyright (C) 2002-2011 - Rob Versluis <r.j.versluis@rocrail.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package net.rocrail.androc.objects;

import android.content.Intent;
import android.view.View;

import net.rocrail.androc.RocrailService;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StageBlock extends Block {
    public boolean ExitClosed;
    public List<SBSection> Sections = new ArrayList<>();
    public HashMap<String, SBSection> SectionMap = new HashMap<>();
    String ExitState;
    String LocoID;
    boolean Reserved;
    boolean Entering;

    public StageBlock(RocrailService rocrailService, Attributes atts) {
        super(rocrailService, atts);
        ExitState = Item.getAttrValue(atts, "exitstate", "open");
        LocoID = Item.getAttrValue(atts, "locid", "");
        Reserved = Item.getAttrValue(atts, "reserved", false);
        Entering = Item.getAttrValue(atts, "entering", false);
        Text = ID;
        Background = true;
        State = Item.getAttrValue(atts, "state", State);
        Closed = State.equals("closed");
        ExitClosed = ExitState.equals("closed");
    }

    public void updateWithAttributes(Attributes atts) {
        ExitState = Item.getAttrValue(atts, "exitstate", ExitState);
        LocoID = Item.getAttrValue(atts, "locid", "");
        Reserved = Item.getAttrValue(atts, "reserved", false);
        Entering = Item.getAttrValue(atts, "entering", false);
        State = Item.getAttrValue(atts, "state", State);
        Closed = State.equals("closed");
        ExitClosed = ExitState.equals("closed");
        updateTextColor();
        super.updateWithAttributes(atts);
    }

    public void updateTextColor() {
        if (State.equals("closed")) {
            Text = "Closed";
            colorName = Item.COLOR_CLOSED;
        } else if (LocoID != null && !LocoID.trim().isEmpty()) {
            Text = LocoID;
            if (Reserved)
                colorName = Item.COLOR_RESERVED;
            else if (Entering)
                colorName = Item.COLOR_ENTER;
            else
                colorName = Item.COLOR_OCCUPIED;
        } else {
            int cnt = 0;
            if (Sections != null) {
                for (SBSection section : Sections) {
                    if (!section.LcID.isEmpty())
                        cnt++;
                }
                Text = ID + "[" + cnt + "] " + (ExitState.equals("closed") ? "<" : "");
                colorName = Item.COLOR_FREE;
            }
        }
    }


    public String getImageName(boolean ModPlan) {
        this.ModPlan = ModPlan;
        int orinr = getOriNr(ModPlan);

        if (orinr % 2 == 0) {
            // vertical
            textVertical = true;
            cX = 1;
            cY = 4;
        } else {
            // horizontal
            cX = 4;
            cY = 1;
            textVertical = false;
        }
        ImageName = "stageblock_" + orinr;

        updateTextColor();

        return ImageName;
    }

    public void addSection(Attributes atts) {
        SBSection section = new SBSection();
        section.ID = Item.getAttrValue(atts, "id", "");
        section.LcID = Item.getAttrValue(atts, "lcid", "");
        Sections.add(section);
        if (section.ID != null && !section.ID.isEmpty())
            SectionMap.put(section.ID, section);
    }

    public void updateSection(Attributes atts) {
        SBSection section = SectionMap.get(Item.getAttrValue(atts, "id", ""));
        if (section != null) {
            section.LcID = Item.getAttrValue(atts, "lcid", "");
            updateTextColor();
        }
    }

    public void onClick(View v) {
        try {
            Intent intent = new Intent(activity, net.rocrail.androc.activities.ActStage.class);
            intent.putExtra("id", StageBlock.this.ID);
            activity.startActivity(intent);
        } catch (Exception e) {
            // invalid activity
        }
    }


    public static class SBSection {
        public String ID = "";
        public String LcID = "";
    }
}
