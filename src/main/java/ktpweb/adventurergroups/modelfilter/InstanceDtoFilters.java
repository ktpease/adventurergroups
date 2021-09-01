package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class InstanceDtoFilters
{

    protected static String[] simpleFilter = { "id", "active", "subdomainName",
            "displayName" };

    protected static String[] fullFilter = { "id", "active", "subdomainName",
            "displayName", "description", "createDate", "lastActivationDate",
            "lastDeactivationDate", "owner", "maintainers", "characters",
            "characterGroups" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("instanceFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("instanceFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter))
        .addFilter("ownerFilter",
            SimpleBeanPropertyFilter
                .filterOutAllExcept(OwnerDtoFilters.simpleFilter))
        .addFilter("maintainerFilter",
            SimpleBeanPropertyFilter
                .filterOutAllExcept(MaintainerDtoFilters.simpleFilter))
        .addFilter("characterFilter",
            SimpleBeanPropertyFilter
                .filterOutAllExcept(CharacterDtoFilters.simpleFilter))
        .addFilter("characterGroupFilter", SimpleBeanPropertyFilter
            .filterOutAllExcept(CharacterGroupDtoFilters.simpleFilter));
}
