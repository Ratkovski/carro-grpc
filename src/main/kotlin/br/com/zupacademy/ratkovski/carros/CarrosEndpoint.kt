package br.com.zupacademy.ratkovski.carros

import br.com.zupacademy.ratkovski.CarroGrpcServiceGrpc
import br.com.zupacademy.ratkovski.CarroRequest
import br.com.zupacademy.ratkovski.CarroResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val repository: CarroRepository):CarroGrpcServiceGrpc.CarroGrpcServiceImplBase() {
    override fun adicionar(request: CarroRequest?, responseObserver: StreamObserver<CarroResponse>?) {


        if (request != null) {
            if (repository.existsByPlaca(request.placa)) {
                responseObserver?.onError(
                    Status.ALREADY_EXISTS
                        .withDescription("carro com placa existente")
                        .asRuntimeException()
                )
                return
            }


            val carro = Carro(
                modelo = request.modelo,
                placa = request.placa
            )

            try {
                repository.save(carro)
            } catch (e: ConstraintViolationException) {
                responseObserver?.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("dados de entrada invalidos")
                        .asRuntimeException()
                )
                return
            }
            responseObserver?.onNext(CarroResponse.newBuilder().setId(carro.id!!).build())
            responseObserver?.onCompleted()
        }
    }
}
