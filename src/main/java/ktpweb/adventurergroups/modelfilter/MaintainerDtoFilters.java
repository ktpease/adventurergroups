package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class MaintainerDtoFilters
{

    protected static String[] simpleFilter = { "id", "isRegistered", "username",
            "displayname", "avatarFilename" };

    protected static String[] fullFilter = { "id", "isRegistered", "username",
            "displayname", "avatarFilename", "email", "inviteToken",
            "createDate", "instance", "characters" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("maintainerFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("maintainerFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter))
        .addFilter("instanceFilter",
            SimpleBeanPropertyFilter
                .filterOutAllExcept(InstanceDtoFilters.simpleFilter))
        .addFilter("characterFilter", SimpleBeanPropertyFilter
            .filterOutAllExcept(CharacterDtoFilters.simpleFilter));
}
