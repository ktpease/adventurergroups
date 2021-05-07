package ktpweb.adventurergroups.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Instance getInstance(Long id) throws Exception
    {
        try
        {
            return instanceRepository.findById(id).orElse(null);
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    public Instance getInstance(InstanceDto instanceDto) throws Exception
    {
        return getInstance(instanceDto.getId());
    }
}
