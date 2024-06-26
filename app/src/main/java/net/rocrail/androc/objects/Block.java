/*
 Rocrail - Model Railroad Software

 Copyright (C) 2002-2010 - Rob Versluis <r.j.versluis@rocrail.net>

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

public class Block extends Item implements View.OnClickListener {
    public String LocoID;
    public boolean Closed = false;
    public boolean Accept;
    boolean Small;

    public Block(RocrailService rocrailService, Attributes atts) {
        super(rocrailService, atts);
        Small = Item.getAttrValue(atts, "smallsymbol", false);
        LocoID = Item.getAttrValue(atts, "locid", ID);
        Reserved = Item.getAttrValue(atts, "reserved", false);
        Entering = Item.getAttrValue(atts, "entering", false);
        Accept = Item.getAttrValue(atts, "acceptident", false);
        Text = LocoID;
        Background = true;
        updateTextColor();
    }

    public boolean isOccupied() {
        return ((colorName == Block.COLOR_OCCUPIED) || (colorName == Block.COLOR_ENTER));
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
            Text = ID;
            if (Accept)
                colorName = Item.COLOR_ACCEPTIDENT;
            else
                colorName = Item.COLOR_FREE;
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
            if (Small) {
                cY = 2;
                ImageName = "block_2_s";
            } else {
                ImageName = "block_2";
            }
        } else {
            // horizontal
            cX = 4;
            cY = 1;
            textVertical = false;
            if (Small) {
                cX = 2;
                ImageName = "block_1_s";
            } else {
                ImageName = "block_1";
            }
        }

        updateTextColor();

        return ImageName;
    }


    public void updateWithAttributes(Attributes atts) {
        LocoID = Item.getAttrValue(atts, "locid", LocoID);
        Reserved = Item.getAttrValue(atts, "reserved", false);
        Entering = Item.getAttrValue(atts, "entering", false);
        State = Item.getAttrValue(atts, "state", State);
        Accept = Item.getAttrValue(atts, "acceptident", Accept);
        Closed = State.equals("closed");
        updateTextColor();
        super.updateWithAttributes(atts);
    }


    public void OpenClose() {
        Closed = !Closed;
        m_RocrailService.sendMessage("bk", String.format("<bk id=\"%s\" state=\"%s\"/>",
                ID, Closed ? "closed" : "open"));
    }

    public void AcceptIdent() {
        m_RocrailService.sendMessage("bk", String.format("<bk id=\"%s\" acceptident=\"%s\"/>",
                ID, "true"));
    }

    public void onClick(View v) {
        try {
            Intent intent = new Intent(activity, net.rocrail.androc.activities.ActBlock.class);
            intent.putExtra("id", Block.this.ID);
            activity.startActivity(intent);
        } catch (Exception e) {
            // invalid activity
        }
    }

    public void propertiesView() {
        try {
            if (Block.this.LocoID != null && m_RocrailService.m_Model.getLoco(Block.this.LocoID) != null) {
                Intent intent = new Intent(activity, net.rocrail.androc.activities.ActLoco.class);
                intent.putExtra("id", Block.this.LocoID);
                intent.putExtra("blockid", Block.this.ID);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            // invalid activity
        }
    }

}
