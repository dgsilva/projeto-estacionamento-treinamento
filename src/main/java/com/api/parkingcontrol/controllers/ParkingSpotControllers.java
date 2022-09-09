package com.api.parkingcontrol.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.dto.ParkingSpotDTO;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-control")
public class ParkingSpotControllers {

	
	final ParkingSpotService parkingSpotService ;

	public ParkingSpotControllers(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}

	@PostMapping
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDTO parkingSpotDTO){
		if(parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar())) {
			return  ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito: A placa neste carro já está em uso.");
		}
		if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber())) {
			return  ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito: Vaga de estacionamento já está em uso!");
		}
		
		if(parkingSpotService.existsByApartamenAndBlock(parkingSpotDTO.getApartament(), parkingSpotDTO.getBlock())) {
			return  ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito:Já tem uma vaga sendo usado neste apartamento/bloco");
		}
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
	}
	
	@GetMapping()
	public ResponseEntity<Page<ParkingSpotModel>> findAll(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));		
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object>getOneParkingSpot(@PathVariable UUID id){
		Optional<ParkingSpotModel> parkSpotModelOptional = parkingSpotService.findById(id);
		if(!parkSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não foi encontrado nenhuma vaga no estacionamento");
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(parkSpotModelOptional.get());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object>deleteParkingSpot(@PathVariable UUID id){
		Optional<ParkingSpotModel> parkSpotModelOptional = parkingSpotService.findById(id);
		if(!parkSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não foi encontrado nenhuma vaga no estacionamento");
		}
		
		parkingSpotService.delete(parkSpotModelOptional.get());
		return ResponseEntity.status(HttpStatus.OK).body("Foi deletado a vaga no estacionamento com sucesso!");
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object>updateParkinSpot(@PathVariable("id")UUID id,
			@RequestBody @Valid ParkingSpotDTO parkingSpotDTO){
		Optional <ParkingSpotModel> parkSpotModelOptional = parkingSpotService.findById(id);
		if(!parkSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não foi encontrado nenhuma vaga no estacionamento");
			
		}
		
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
		parkingSpotModel.setId(parkSpotModelOptional.get().getId());
		parkingSpotModel.setRegistrationDate(parkSpotModelOptional.get().getRegistrationDate());
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
	}
	
}
