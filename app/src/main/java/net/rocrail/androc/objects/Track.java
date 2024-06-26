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


import net.rocrail.androc.RocrailService;

import org.xml.sax.Attributes;


public class Track extends Item {

    public Track(RocrailService rocrailService, Attributes atts) {
        super(rocrailService, atts);
    }

    public void updateWithAttributes(Attributes atts) {
        super.updateWithAttributes(atts);
    }

    public String getImageName(boolean ModPlan) {
        this.ModPlan = ModPlan;
        int orinr = getOriNr(ModPlan);

        if (!BlockID.isEmpty()) {
            Block bk = m_RocrailService.m_Model.m_BlockMap.get(BlockID);
            if (bk != null) Occupied = bk.isOccupied();
            else {
                Sensor fb = m_RocrailService.m_Model.m_SensorMap.get(BlockID);
                if (fb != null) Occupied = fb.State.equals("true");
            }
        }

        String suffix = "";
        if (RouteLocked) suffix = "_route";
        if (Occupied) suffix = "_occ";

        switch (Type) {
            case "curve" -> ImageName = String.format("curve%s_%d", suffix, orinr);
            case "buffer", "connector", "dir" -> {
                // symbol naming fix (see rocrail/impl/pclient.c line 250)
                if (orinr == 1) orinr = 3;
                else if (orinr == 3) orinr = 1;
                ImageName = String.format("%s%s_%d", Type, suffix, orinr);
            }
            case "concurveright" -> {
                if (orinr == 2) orinr = 4;
                else if (orinr == 4) orinr = 2;
                ImageName = String.format("connector_curve_right%s_%d", suffix, orinr);
            }
            case "concurveleft" -> {
                if (orinr == 2) orinr = 4;
                else if (orinr == 4) orinr = 2;
                ImageName = String.format("connector_curve_left%s_%d", suffix, orinr);
            }
            case "dirall" ->
                    ImageName = String.format("%s%s_%d", Type, suffix, (orinr % 2 == 0 ? 2 : 1));
            default -> ImageName = String.format("track%s_%d", suffix, (orinr % 2 == 0 ? 2 : 1));
        }

        //System.out.println("ID="+ID+", ImageName="+ImageName);
        return ImageName;
    }
}
