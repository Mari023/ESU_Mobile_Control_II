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

import android.view.View;

import net.rocrail.androc.RocrailService;

import org.xml.sax.Attributes;

public class Route extends Item implements View.OnClickListener {

    public Route(RocrailService rocrailService, Attributes atts) {
        super(rocrailService, atts);
        Show = getAttrValue(atts, "show", false);
    }

    public void onClick(View v) {
        flip();
    }


    public void flip() {
        m_RocrailService.sendMessage("st", String.format("<st id=\"%s\" cmd=\"test\"/>", ID));
    }


    public String getImageName(boolean ModPlan) {
        this.ModPlan = ModPlan;
        int orinr = getOriNr(ModPlan);
        ImageName = String.format("route_%d", (orinr % 2 == 0 ? 2 : 1));
        return ImageName;
    }
}
