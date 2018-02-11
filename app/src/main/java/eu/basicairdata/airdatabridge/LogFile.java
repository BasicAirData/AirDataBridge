/**
 * LogFile - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 19/1/2018
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.basicairdata.airdatabridge;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.StringTokenizer;

class LogFile implements Comparable<LogFile> {

    public static final short LOCATION_UNSPECIFIED = 0;
    public static final short LOCATION_REMOTE = 1;
    public static final short LOCATION_LOCAL = 2;

    public short Location = LOCATION_UNSPECIFIED;
    public String Name;                 // The name of the log file
    public String LocalName;            // The name of the file when locally archived
    public String Extension;            // The name of the log file
    public String DateTime;             // The formatted datetime
    public String Sizekb;               // The formatted size (in kb)

    public boolean Current = false;     // True if the file is the current log file

    long   ldatetime;                   // The numerical datetime
    long   lsize;                       // The numerical size in bytes

    LogFile() {}

    LogFile(String name, String size, String datetime) {            // USE FOR REMOTE
        Location = LOCATION_REMOTE;
        StringTokenizer tokens = new StringTokenizer(name, ".");
        Name = tokens.hasMoreTokens() ? tokens.nextToken() : "";
        Extension = tokens.hasMoreTokens() ? tokens.nextToken() : "";

        lsize = Long.parseLong(size);
        Sizekb = String.format("%d", (long)Math.ceil((float)lsize / 1024f)) + " kb";

        ldatetime = Long.parseLong(datetime) * 1000;
        String vv = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(ldatetime);
        LocalName = vv + "-" + size + "-" + Name;

        DateTime = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.ENGLISH).format(ldatetime);
    }

    LogFile(String localname) {                                     // USE FOR LOCAL
        Location = LOCATION_LOCAL;
        StringTokenizer tokens = new StringTokenizer(localname, "-");
        String sdate = tokens.hasMoreTokens() ? tokens.nextToken() : "";
        try {
            ldatetime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).parse(sdate).getTime();
        } catch (ParseException e) {
            ldatetime = 0;
        }
        DateTime = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.ENGLISH).format(ldatetime);

        String ssize = tokens.hasMoreTokens() ? tokens.nextToken() : "";
        if (!ssize.isEmpty()) {
            lsize = Long.parseLong(ssize);
            Sizekb = String.format("%d", (long) Math.ceil((float) lsize / 1024f)) + " kb";
        } else {
            lsize = 0;
            Sizekb = "-";
        }

        Name = tokens.hasMoreTokens() ? tokens.nextToken(".").substring(1) : "";
        Extension = tokens.hasMoreTokens() ? tokens.nextToken() : "";

        tokens = new StringTokenizer(localname, ".");
        LocalName = tokens.hasMoreTokens() ? tokens.nextToken() : "";
    }

    void setSize(String size) {
        lsize = Long.parseLong(size);
        Sizekb = String.format("%d", (long)Math.ceil((float)lsize / 1024f)) + " kb";
    }

    void setDatetime(String datetime) {
        ldatetime = Long.parseLong(datetime) * 1000;
        String vv = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(ldatetime);
        LocalName = vv + "-" + lsize + "-" + Name;

        DateTime = new SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale.ENGLISH).format(ldatetime);
    }

    @Override
    public int compareTo(LogFile f) {

        if (ldatetime > f.ldatetime) {
            return -1;
        }
        else if (ldatetime < f.ldatetime) {
            return 1;
        }
        else {
            int i = Name.compareToIgnoreCase(f.Name);
            return i;
        }

    }

}
