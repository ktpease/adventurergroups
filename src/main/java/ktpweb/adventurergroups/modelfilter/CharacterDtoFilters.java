package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class CharacterDtoFilters
{

    protected static String[] simpleFilter = { "id", "name", "description",
            "colorPrimary", "colorSecondary", };

    protected static String[] fullFilter = { "id", "name", "description",
            "colorPrimary", "colorSecondary", "maintainer", "createDate",
            "instance", "characterGroup" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("characterFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("characterFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter))
        .addFilter("maintainerFilter",
            SimpleBeanPropertyFilter
                .filterOutAllExcept(MaintainerDtoFilters.simpleFilter))
        .addFilter("instanceFilter",
            SimpleBeanPropertyFilter
                .filterOutAllExcept(InstanceDtoFilters.simpleFilter))
        .addFilter("characterGroupFilter", SimpleBeanPropertyFilter
            .filterOutAllExcept(CharacterGroupDtoFilters.simpleFilter));
}
