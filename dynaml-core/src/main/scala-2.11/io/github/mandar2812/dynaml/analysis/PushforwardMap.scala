/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
* */
package io.github.mandar2812.dynaml.analysis

import io.github.mandar2812.dynaml.pipes.{DataPipe, Encoder}
import io.github.mandar2812.dynaml.probability.{ContinuousDistrRV, MeasurableDistrRV}
import spire.algebra.Field

/**
  * @author mandar2812 on 22/12/2016.
  *
  * Push forward map is a function that has a well defined inverse
  * as well as Jacobian of the inverse.
  */
abstract class PushforwardMap[
@specialized(Double) Source,
@specialized(Double) Destination,
@specialized(Double) Jacobian](
  implicit detImpl: DataPipe[Jacobian, Double], field: Field[Destination])
  extends Encoder[Source, Destination] { self =>

  /**
    * Represents the decoding/inverse operation.
    */
  override val i: DifferentiableMap[Destination, Source, Jacobian]

  def ->[R <: ContinuousDistrRV[Source]](r: R): MeasurableDistrRV[Source, Destination, Jacobian] =
    new MeasurableDistrRV[Source, Destination, Jacobian](r)(self)

  //def ->[P <: ContinuousProcess[_, _, ]]

  /**
    * Return the implementation used for calculating
    * determinant of the inverse function's Jacobian
    * */
  def _det = detImpl

}

object PushforwardMap {
  def apply[S, D, J](forward: DataPipe[S, D], reverse: DifferentiableMap[D, S, J])(
    implicit detImpl: DataPipe[J, Double], field: Field[D]) =
    new PushforwardMap[S, D, J] {

      /**
        * Represents the decoding/inverse operation.
        */
      override val i = reverse

      override def run(data: S) = forward(data)
    }
}
