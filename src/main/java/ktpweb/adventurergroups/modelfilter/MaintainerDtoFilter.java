package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class MaintainerDtoFilter
{

    private static String[] simpleFilter = { "id", "isRegistered", "username",
            "displayname", "avatarFilename" };

    private static String[] fullFilter = { "id", "isRegistered", "username",
            "displayname", "avatarFilename", "email", "inviteToken",
            "createDate", "instance", "characters" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter));
}
