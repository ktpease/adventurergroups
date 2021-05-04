package ktpweb.adventurergroups.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import ktpweb.adventurergroups.entity.Instance;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.repository.InstanceRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstanceService
{
    @Autowired
    private InstanceRepository instanceRepository;

    public Instance getInstance(InstanceDto instanceDto)
    {
        try
        {
            return instanceRepository.findById(instanceDto.getId())
                .orElse(null);
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            return null;
        }
    }
}
