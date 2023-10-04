package spring.com.rest.api.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import spring.com.rest.api.dto.AddressDto;
import spring.com.rest.api.dto.ContactDTO;
import spring.com.rest.api.dto.UserDto;
import spring.com.rest.api.entity.AddressEntity;

import spring.com.rest.api.entity.UserEntity;
import spring.com.rest.api.exceptions.ResourceNotFoundException;
import spring.com.rest.api.repository.ContactRepository;
import spring.com.rest.api.repository.UserRepository;
import spring.com.rest.api.entity.*;
@Service
public class UserService {
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private EmailService emailService;
	@Autowired
	private ContactRepository contactRepo;

	public UserEntity createUser(UserDto userdto) {
	    Optional<UserEntity> existingUser = userRepo.findByName(userdto.getName());

	    if (existingUser.isEmpty()) {
	        UserEntity userEntity = new UserEntity();
	        userEntity.setId(userdto.getId());
	        userEntity.setName(userdto.getName());
	        userEntity.setEmail(userdto.getEmail());
	        userEntity.setGender(userdto.getGender());
	        userEntity.setNationality(userdto.getNationality());
	        
	        

	        AddressEntity address = new AddressEntity();
	        address.setId(userdto.getAddress().getId());
	        address.setCity(userdto.getAddress().getCity());
	        address.setState(userdto.getAddress().getState());
	        userEntity.setAddress(address);

	        userEntity = userRepo.save(userEntity);
	        
	        if (userdto.getContacts() != null) {
                for (ContactDTO contactDto : userdto.getContacts()) {
                    ContactEntity contact = new ContactEntity();
                    contact.setPhone(contactDto.getPhone());
                    contact.setUser(userEntity); // Set the user for the contact
                    contactRepo.save(contact);
                }
            }
	        

	        String subject = "Welcome to our application!";
	        String message = "Dear " + userdto.getName() + ",\n\nWelcome to our application! We are glad to have you on board.";
	        emailService.sendEmail(userdto.getEmail(), subject, message);

	        return userEntity;
	    } else {
	        UserEntity existingUserEntity = existingUser.get();
	        String subject = "Welcome back to our application!";
	        String message = "Dear " + existingUserEntity.getName() + ",\n\nWelcome back to our application! We are glad to have you on board.";
	        emailService.sendEmail(existingUserEntity.getEmail(), subject, message);

	        return existingUserEntity;
	    }
	
	}
	

	
	public UserEntity updateUser(UserDto userdto, int id) {
		UserEntity userEntity=userRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("User", "id", id));

		userEntity.setId(userdto.getId());
		userEntity.setName(userdto.getName());
		userEntity.setEmail(userdto.getEmail());
		userEntity.setGender(userdto.getGender());
		
		
		return userRepo.save(userEntity);

		
		}

	

    public UserDto getUserById(int id) {
       Optional<UserEntity> userEntity =userRepo.findById(id);
       return userEntity.map(x ->{
    	   UserDto user = new UserDto();
            user.setId(x.getId());
            user.setName(x.getName());
            user.setEmail(x.getEmail());
            user.setGender(x.getGender());
            user.setNationality(x.getNationality());
            
            
            return user;
        }).orElseThrow(()->new ResourceNotFoundException("User", "id", id));
    }

	
    public List<UserDto> getAllUsers() {
        List<UserEntity> userEntityList = userRepo.findAll();
        List<UserDto> userDtoList = userEntityList.stream().map(user -> {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setName(user.getName());
            userDto.setEmail(user.getEmail());
            userDto.setGender(user.getGender());
            userDto.setNationality(user.getNationality());
            
            
            List<ContactDTO> contactDtoList = user.getContacts().stream().map(contactEntity -> {
                ContactDTO contactDto = new ContactDTO();
                contactDto.setId(contactEntity.getId());
                contactDto.setPhone(contactEntity.getPhone());
                return contactDto;
            }).collect(Collectors.toList());

            userDto.setContacts(contactDtoList);

            AddressDto addDto = new AddressDto();
            AddressEntity addressEntity = user.getAddress();
            if (addressEntity != null) {
                addDto.setId(addressEntity.getId());
                addDto.setCity(addressEntity.getCity());
                addDto.setState(addressEntity.getState());
            } else {
                // Handle the case when the address is null
                // You can set default values or leave the fields empty in the AddressDto
                addDto.setId(0);
                addDto.setCity("");
                addDto.setState("");
            }
            
            userDto.setAddress(addDto);
            

          return userDto; 
        }).collect(Collectors.toList());
        
        return userDtoList;
    }


	
	public void deleteUser(int id) {
		userRepo.deleteById(id);
	}

	


	 public int getCountOUsers() {
	        return userRepo.getCountOfUsers();
	    }
	  

}
