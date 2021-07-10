package br.com.zupacademy.ratkovski.carros

import br.com.zupacademy.ratkovski.CarroGrpcServiceGrpc
import br.com.zupacademy.ratkovski.CarroRequest

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import javax.inject.Singleton


/**
 * 1. happy path -ok
 * 2.quando já existe um carro com a placa-ok
 * 3.quando os dados encontrados são invalidos
 */
@MicronautTest(transactional = false)
internal class CarrotsEndpointTest(val repository: CarroRepository,val grpcClient:CarroGrpcServiceGrpc.CarroGrpcServiceBlockingStub) {

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deve inserir um novo carro`() {
        //cenario
        //repository.deleteAll()
        //acao
        val response = grpcClient.adicionar(
            CarroRequest.newBuilder()
                .setModelo("gol")
                .setPlaca("HPX-1234")
                .build()
        )
        //validacao
//        assertNotNull(response.id)
//
        with(response) {
            assertNotNull(id)
            assertTrue(repository.existsById(this.id))//efeito colateral
        }
    }


    @Test
    fun `não deve adicionar novo carro quando carro com placa já existente`() {
        //cenario
      //  repository.deleteAll()
        val existente = repository.save(Carro(modelo = "gol", placa = "OIP-9876"))

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                    .setModelo("Ferrari")
                    .setPlaca(existente.placa)
                    .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("carro com placa existente", status.description)
        }
    }

    @Test
    fun `não deve adicionar novo carro quando dados de entrada forem inválidos`() {
        //cenario
      //  repository.deleteAll()

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                     .setModelo("")
                     .setPlaca("")
                    .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("dados de entrada inválidos", status.description)
            // TODO: verificar as violações da bean validations
        }

    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarroGrpcServiceGrpc.CarroGrpcServiceBlockingStub? {
            return CarroGrpcServiceGrpc.newBlockingStub(channel)
        }

    }
}