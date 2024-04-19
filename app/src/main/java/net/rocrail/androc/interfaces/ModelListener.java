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
package net.rocrail.androc.interfaces;

public interface ModelListener {
    int MODELLIST_PLAN_START = 100;
    int MODELLIST_PLAN = 0;
    int MODELLIST_LC = 1;
    int MODELLIST_TK = 2;
    int MODELLIST_BK = 3;
    int MODELLIST_FB = 4;
    int MODELLIST_ST = 5;
    int MODELLIST_SC = 6;
    int MODELLIST_SW = 7;
    int MODELLIST_SG = 8;
    int MODELLIST_CO = 9;
    int MODELLIST_TX = 10;

    void modelListLoaded(int MODELLIST);

    void modelUpdate(int MODELLIST, String ID);
}
