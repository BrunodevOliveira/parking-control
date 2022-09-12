package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController //Bin que indica o uso de uma API rest
@CrossOrigin(origins = "*", maxAge = 3600) //Permite que seja acessado de qualquer fonte
@RequestMapping("/parking-spot")//Defino a URI a nível de classe
public class ParkingSpotController {
	//Camada que recebe as solicitações HTTP e aciona o service para que ele acione o repository e faça as transações com o BD
	
	//Crio a injeção de dependências do Service via construtor
	final ParkingSpotService parkingSpotService;

	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}
	
	@PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){
		//FAço algumas validações | crio os métodos no Service e implemento eles no SpotRepository
		if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot already registered for this apartment/block!");
        }
		
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel); //Faz a conversão de DTO para o modelo que será salvo no BD
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel)); 
        //Retorno uma resposta com o status HTTP da requisição e no bodyeu passo o retorno do método save com os dados dalvos no bd
    }
	
	  @GetMapping
	    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
	        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
	    }
	  //@PageableDefault-> Definos valores default caso na requisição n seja enviado.
	
	  @GetMapping("/{id}")
	    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
	        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
	        if (!parkingSpotModelOptional.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
	        }
	        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
	    }
	  
	  @DeleteMapping("/{id}")
	    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id){
	        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
	        if (!parkingSpotModelOptional.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
	        }
	        parkingSpotService.delete(parkingSpotModelOptional.get());
	        return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully.");
	    }
	  
	  @PutMapping("/{id}")
	    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
	                                                    @RequestBody @Valid ParkingSpotDto parkingSpotDto){ //Recebo o ID e os campos a serem editados e validados novamente
	        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
	        if (!parkingSpotModelOptional.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
	        }
	        
	        //Passo o DTO para Model e seto apenas o ID e a data de Regitro que já existem no BD
	        var parkingSpotModel = new ParkingSpotModel();
	        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
	        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
	        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
	        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
	    }
	  
}
