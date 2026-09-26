
import { useState } from 'react'
import '../App.css'
import axios from 'axios'

function Configuracion() {

  const [seccion, setSeccion] = useState("")
  const [vistaFactoring, setVistaFactoring] = useState("")

  // ================================
  // INFORMACIÓN DE LA ORGANIZACIÓN
  // ================================

  const [nombreEmpresa, setNombreEmpresa] = useState('')
  const [moneda, setMoneda] = useState('PEN')
  const [umbralMontoRelevante, setUmbralMontoRelevante] = useState('')

  const [cantidadColaboradores, setCantidadColaboradores] = useState('')
  const [areasCriticas, setAreasCriticas] = useState('')
  const [usuariosAreasCriticas, setUsuariosAreasCriticas] = useState('')
  const [informacionCritica, setInformacionCritica] = useState('')

  // ================================
  // CONTEXTO OPERATIVO
  // ================================

  const [documentosRecibidosDiariamente, setDocumentosRecibidosDiariamente] = useState('')
  const [promedioOperacionesDiarias, setPromedioOperacionesDiarias] = useState('')

  // ================================
  // CLIENTES
  // ================================

  const [razonSocialCliente, setRazonSocialCliente] = useState('')
  const [rucCliente, setRucCliente] = useState('')
  const [clienteVip, setClienteVip] = useState(false)

  // ================================
  // DEUDORES
  // ================================

  const [razonSocialDeudor, setRazonSocialDeudor] = useState('')
  const [rucDeudor, setRucDeudor] = useState('')

  // ================================
  // FACTURAS
  // ================================

  const [numeroFactura, setNumeroFactura] = useState('')
  const [rucClienteFactura, setRucClienteFactura] = useState('')
  const [rucDeudorFactura, setRucDeudorFactura] = useState('')
  const [montoFactura, setMontoFactura] = useState('')
  const [monedaFactura, setMonedaFactura] = useState('PEN')
  const [estadoFactura, setEstadoFactura] = useState('Pendiente')

  // ================================
  // PERFIL DE RIESGO
  // ================================

  const [tieneClientesVip, setTieneClientesVip] = useState("")

  const [perdidaVipMin, setPerdidaVipMin] = useState('')
  const [perdidaVipMax, setPerdidaVipMax] = useState('')

  const [perdidaNoVipMin, setPerdidaNoVipMin] = useState('')
  const [perdidaNoVipMax, setPerdidaNoVipMax] = useState('')

  const [situacionesRiesgosas, setSituacionesRiesgosas] = useState('')

  // Archivo actualmente seleccionado
  const [archivoClientesVip, setArchivoClientesVip] = useState(null)

  // Indica si ya se realizó correctamente la importación
  const [clientesVipImportados, setClientesVipImportados] = useState(false)


  // ================================
  // GUARDAR CONFIGURACIÓN
  // ================================

  const guardarConfiguracion = async () => {

    // --------------------------------
    // VALIDAR CAMPOS GENERALES
    // --------------------------------

    if (
      !nombreEmpresa.trim() ||
      !moneda ||
      !umbralMontoRelevante ||
      !cantidadColaboradores ||
      !areasCriticas.trim() ||
      !usuariosAreasCriticas.trim() ||
      !informacionCritica.trim() ||
      !documentosRecibidosDiariamente.trim() ||
      !promedioOperacionesDiarias ||
      !perdidaVipMin ||
      !perdidaVipMax ||
      !perdidaNoVipMin ||
      !perdidaNoVipMax ||
      !situacionesRiesgosas.trim()
    ) {
      alert("Completa todos los campos obligatorios.")
      return
    }

    // --------------------------------
    // VALIDAR SI TIENE CLIENTES VIP
    // --------------------------------

    if (!tieneClientesVip) {
      alert("Indica si la empresa tiene clientes VIP.")
      return
    }

    // --------------------------------
    // SI TIENE VIP, DEBE IMPORTAR EXCEL
    // --------------------------------

    if (tieneClientesVip === "si" && !clientesVipImportados) {
      alert("Debes importar el Excel de clientes VIP antes de guardar la configuración.")
      return
    }

    // --------------------------------
    // VALIDAR RANGO VIP
    // --------------------------------

    if (Number(perdidaVipMax) < Number(perdidaVipMin)) {
      alert(
        "La pérdida máxima de clientes VIP no puede ser menor que la mínima."
      )
      return
    }

    // --------------------------------
    // VALIDAR RANGO NO VIP
    // --------------------------------

    if (Number(perdidaNoVipMax) < Number(perdidaNoVipMin)) {
      alert(
        "La pérdida máxima de clientes o áreas no VIP no puede ser menor que la mínima."
      )
      return
    }

    // --------------------------------
    // ENVIAR CONFIGURACIÓN AL BACKEND
    // --------------------------------

    try {

      const respuesta = await axios.post(
        "http://localhost:8080/configuracion",
        {
          nombreEmpresa: nombreEmpresa,
          moneda: moneda,
          umbralMontoRelevante: Number(umbralMontoRelevante),

          cantidadColaboradores: Number(cantidadColaboradores),
          areasCriticas: areasCriticas,
          usuariosAreasCriticas: usuariosAreasCriticas,
          informacionCritica: informacionCritica,

          documentosRecibidosDiariamente:
            documentosRecibidosDiariamente,

          promedioOperacionesDiarias:
            Number(promedioOperacionesDiarias),

          // "si" o "no"
          clientesVip: tieneClientesVip,

          perdidaVipMin:
            Number(perdidaVipMin),

          perdidaVipMax:
            Number(perdidaVipMax),

          perdidaNoVipMin:
            Number(perdidaNoVipMin),

          perdidaNoVipMax:
            Number(perdidaNoVipMax),

          situacionesRiesgosas:
            situacionesRiesgosas
        }
      )

      console.log(
        "Configuración guardada:",
        respuesta.data
      )

      alert("Configuración guardada correctamente.")

    } catch (error) {

      console.error(
        "Error al guardar configuración:",
        error
      )

      alert("No se pudo guardar la configuración.")
    }
  }


  // ================================
  // GUARDAR CLIENTE
  // ================================

  const guardarCliente = async () => {

    if (
      !razonSocialCliente.trim() ||
      !rucCliente.trim()
    ) {
      alert("Completa la razón social y el RUC.")
      return
    }

    try {

      const respuesta = await axios.post(
        "http://localhost:8080/clientes",
        {
          razonSocial: razonSocialCliente,
          ruc: rucCliente,
          vip: clienteVip
        }
      )

      console.log(
        "Cliente guardado:",
        respuesta.data
      )

      alert("Cliente guardado correctamente.")

      setRazonSocialCliente('')
      setRucCliente('')
      setClienteVip(false)

    } catch (error) {

      console.error(
        "Error al guardar cliente:",
        error
      )

      alert("No se pudo guardar el cliente.")
    }
  }


  // ================================
  // GUARDAR DEUDOR
  // ================================

  const guardarDeudor = async () => {

    if (
      !razonSocialDeudor.trim() ||
      !rucDeudor.trim()
    ) {
      alert("Completa la razón social y el RUC.")
      return
    }

    try {

      const respuesta = await axios.post(
        "http://localhost:8080/deudores",
        {
          razonSocial: razonSocialDeudor,
          ruc: rucDeudor
        }
      )

      console.log(
        "Deudor guardado:",
        respuesta.data
      )

      alert("Deudor guardado correctamente.")

      setRazonSocialDeudor('')
      setRucDeudor('')

    } catch (error) {

      console.error(
        "Error al guardar deudor:",
        error
      )

      alert("No se pudo guardar el deudor.")
    }
  }


  // ================================
  // GUARDAR FACTURA
  // ================================

  const guardarFactura = async () => {

    if (
      !numeroFactura.trim() ||
      !rucClienteFactura.trim() ||
      !rucDeudorFactura.trim() ||
      !montoFactura ||
      !monedaFactura ||
      !estadoFactura
    ) {
      alert("Completa todos los campos de la factura.")
      return
    }

    try {

      const respuesta = await axios.post(
        "http://localhost:8080/facturas",
        {
          numeroFactura: numeroFactura,
          rucCliente: rucClienteFactura,
          rucDeudor: rucDeudorFactura,
          monto: Number(montoFactura),
          moneda: monedaFactura,
          estado: estadoFactura
        }
      )

      console.log(
        "Factura guardada:",
        respuesta.data
      )

      alert("Factura guardada correctamente.")

      setNumeroFactura('')
      setRucClienteFactura('')
      setRucDeudorFactura('')
      setMontoFactura('')
      setMonedaFactura('PEN')
      setEstadoFactura('Pendiente')

    } catch (error) {

      console.error(
        "Error al guardar factura:",
        error
      )

      alert("No se pudo guardar la factura.")
    }
  }


  // ================================
  // IMPORTAR EXCEL CLIENTES VIP
  // ================================

  const importarClientesVip = async () => {

    if (!archivoClientesVip) {
      alert("Selecciona un archivo Excel.")
      return
    }

    const datos = new FormData()

    datos.append(
      "archivo",
      archivoClientesVip
    )

    try {

      const respuesta = await axios.post(
        "http://localhost:8080/clientes/importar-vip",
        datos,
        {
          headers: {
            "Content-Type": "multipart/form-data"
          }
        }
      )

      console.log(
        "Importación:",
        respuesta.data
      )

      alert(respuesta.data)

      // IMPORTANTE:
      // Indicamos que el Excel ya fue importado
      setClientesVipImportados(true)

      // Limpiamos solamente el archivo seleccionado
      setArchivoClientesVip(null)

    } catch (error) {

      console.error(
        "Error al importar clientes VIP:",
        error
      )

      // Si falla la importación,
      // seguimos considerando que no se importó
      setClientesVipImportados(false)

      alert(
        "No se pudo importar el archivo Excel."
      )
    }
  }


  return (

    <div className="configuracion-container">

      {/* ===================================== */}
      {/* CABECERA */}
      {/* ===================================== */}

      <div className="configuracion-header">

        <div>
          <h1>Configuración</h1>

          <p>
            Configura los parámetros de seguridad,
            organización y contexto de factoring.
          </p>
        </div>

      </div>


      {/* ===================================== */}
      {/* MENÚ PRINCIPAL */}
      {/* ===================================== */}

      <div className="configuracion-tabs">

        <button
          className={
            seccion === "integraciones"
              ? "tab-activa"
              : ""
          }
          onClick={() =>
            setSeccion("integraciones")
          }
        >
          Integraciones
        </button>

        <button
          className={
            seccion === "usuarios"
              ? "tab-activa"
              : ""
          }
          onClick={() =>
            setSeccion("usuarios")
          }
        >
          Usuarios y Roles
        </button>

        <button
          className={
            seccion === "asistente"
              ? "tab-activa"
              : ""
          }
          onClick={() =>
            setSeccion("asistente")
          }
        >
          Asistente de datos
        </button>

      </div>


      {/* ===================================== */}
      {/* INTEGRACIONES */}
      {/* ===================================== */}

      {seccion === "integraciones" && (

        <div className="configuracion-card">

          <h2>Integraciones</h2>

          <p>
            Configura las plataformas que utilizará
            el sistema para analizar los eventos.
          </p>

          <div className="asistente-campo">

            <label>
              Microsoft 365
            </label>

            <button type="button">
              Conectar
            </button>

          </div>

          <div className="asistente-campo">

            <label>
              Google Workspace
            </label>

            <button type="button">
              Conectar
            </button>

          </div>

        </div>

      )}


      {/* ===================================== */}
      {/* USUARIOS Y ROLES */}
      {/* ===================================== */}

      {seccion === "usuarios" && (

        <div className="configuracion-card">

          <h2>Usuarios y Roles</h2>

          <p>
            Administra los usuarios y permisos
            de la plataforma.
          </p>

        </div>

      )}


      {/* ===================================== */}
      {/* ASISTENTE DE DATOS */}
      {/* ===================================== */}

      {seccion === "asistente" && (

        <div className="asistente-datos">

          <div className="asistente-header">

            <h2>
              Asistente de datos
            </h2>

            <p>
              Responde las siguientes preguntas para
              personalizar el análisis y mejorar la
              detección de riesgos según el contexto
              de tu organización.
            </p>

          </div>


          {/* ===================================== */}
          {/* INFORMACIÓN DE LA ORGANIZACIÓN */}
          {/* ===================================== */}

          <div className="configuracion-card">

            <h2>
              Información de la organización
            </h2>


            {/* NOMBRE EMPRESA */}

            <div className="asistente-campo">

              <label>
                Nombre de la empresa *
              </label>

              <input
                type="text"
                value={nombreEmpresa}
                onChange={(e) =>
                  setNombreEmpresa(e.target.value)
                }
                placeholder="Ejemplo: Factoring ABC S.A.C."
              />

            </div>


            {/* MONEDA */}

            <div className="asistente-campo">

              <label>
                Moneda *
              </label>

              <select
                value={moneda}
                onChange={(e) =>
                  setMoneda(e.target.value)
                }
              >

                <option value="PEN">
                  Sol peruano (PEN)
                </option>

                <option value="USD">
                  Dólar estadounidense (USD)
                </option>

              </select>

            </div>


            {/* UMBRAL */}

            <div className="asistente-campo">

              <label>
                Umbral de monto relevante *
              </label>

              <input
                type="number"
                min="0"
                value={umbralMontoRelevante}
                onChange={(e) =>
                  setUmbralMontoRelevante(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: 50000"
              />

            </div>


            {/* COLABORADORES */}

            <div className="asistente-campo">

              <label>
                ¿Cuántos colaboradores tiene la empresa? *
              </label>

              <input
                type="number"
                min="1"
                value={cantidadColaboradores}
                onChange={(e) =>
                  setCantidadColaboradores(
                    e.target.value
                  )
                }
                placeholder="Ingrese la cantidad"
              />

            </div>


            {/* ÁREAS CRÍTICAS */}

            <div className="asistente-campo">

              <label>
                ¿Cuáles son las áreas críticas de la organización? *
              </label>

              <textarea
                value={areasCriticas}
                onChange={(e) =>
                  setAreasCriticas(e.target.value)
                }
                placeholder="Ejemplo: Finanzas, TI, Contabilidad"
              />

            </div>


            {/* USUARIOS */}

            <div className="asistente-campo">

              <label>
                ¿Qué usuarios pertenecen a las áreas críticas? *
              </label>

              <textarea
                value={usuariosAreasCriticas}
                onChange={(e) =>
                  setUsuariosAreasCriticas(
                    e.target.value
                  )
                }
                placeholder="Ingrese los usuarios"
              />

            </div>


            {/* INFORMACIÓN CRÍTICA */}

            <div className="asistente-campo">

              <label>
                ¿Qué información considera crítica? *
              </label>

              <textarea
                value={informacionCritica}
                onChange={(e) =>
                  setInformacionCritica(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: facturas, cuentas bancarias, datos de clientes"
              />

            </div>

          </div>


          {/* ===================================== */}
          {/* CONTEXTO OPERATIVO */}
          {/* ===================================== */}

          <div className="configuracion-card">

            <h2>
              Contexto operativo
            </h2>


            {/* DOCUMENTOS */}

            <div className="asistente-campo">

              <label>
                ¿Qué tipo de documentos recibe diariamente? *
              </label>

              <textarea
                value={documentosRecibidosDiariamente}
                onChange={(e) =>
                  setDocumentosRecibidosDiariamente(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: facturas, órdenes de compra, contratos, estados de cuenta"
              />

            </div>


            {/* OPERACIONES */}

            <div className="asistente-campo">

              <label>
                Promedio de operaciones al día *
              </label>

              <input
                type="number"
                min="0"
                value={promedioOperacionesDiarias}
                onChange={(e) =>
                  setPromedioOperacionesDiarias(
                    e.target.value
                  )
                }
                placeholder="Ingrese la cantidad"
              />

            </div>

          </div>


          {/* ===================================== */}
          {/* CONTEXTO FACTORING */}
          {/* ===================================== */}

          <div className="configuracion-card">

            <h2>
              Contexto factoring
            </h2>

            <p>
              Administra la información relacionada
              con clientes, deudores y facturas.
            </p>


            <div className="factoring-opciones">

              <button
                type="button"
                onClick={() =>
                  setVistaFactoring("clientes")
                }
              >
                Clientes
              </button>

              <button
                type="button"
                onClick={() =>
                  setVistaFactoring("deudores")
                }
              >
                Deudores
              </button>

              <button
                type="button"
                onClick={() =>
                  setVistaFactoring("facturas")
                }
              >
                Facturas
              </button>

            </div>


            {/* ================================= */}
            {/* CLIENTES */}
            {/* ================================= */}

            {vistaFactoring === "clientes" && (

              <div className="factoring-form">

                <h3>
                  Registrar cliente
                </h3>


                <div className="asistente-campo">

                  <label>
                    Razón social *
                  </label>

                  <input
                    type="text"
                    value={razonSocialCliente}
                    onChange={(e) =>
                      setRazonSocialCliente(
                        e.target.value
                      )
                    }
                    placeholder="Razón social"
                  />

                </div>


                <div className="asistente-campo">

                  <label>
                    RUC *
                  </label>

                  <input
                    type="text"
                    value={rucCliente}
                    onChange={(e) =>
                      setRucCliente(
                        e.target.value
                      )
                    }
                    placeholder="RUC"
                  />

                </div>


                <div className="asistente-campo">

                  <label>
                    ¿Es cliente VIP?
                  </label>

                  <select
                    value={clienteVip}
                    onChange={(e) =>
                      setClienteVip(
                        e.target.value === "true"
                      )
                    }
                  >

                    <option value="false">
                      No
                    </option>

                    <option value="true">
                      Sí
                    </option>

                  </select>

                </div>


                <button
                  type="button"
                  onClick={guardarCliente}
                >
                  Guardar cliente
                </button>

              </div>

            )}


            {/* ================================= */}
            {/* DEUDORES */}
            {/* ================================= */}

            {vistaFactoring === "deudores" && (

              <div className="factoring-form">

                <h3>
                  Registrar deudor
                </h3>


                <div className="asistente-campo">

                  <label>
                    Razón social *
                  </label>

                  <input
                    type="text"
                    value={razonSocialDeudor}
                    onChange={(e) =>
                      setRazonSocialDeudor(
                        e.target.value
                      )
                    }
                    placeholder="Razón social"
                  />

                </div>


                <div className="asistente-campo">

                  <label>
                    RUC *
                  </label>

                  <input
                    type="text"
                    value={rucDeudor}
                    onChange={(e) =>
                      setRucDeudor(
                        e.target.value
                      )
                    }
                    placeholder="RUC"
                  />

                </div>


                <button
                  type="button"
                  onClick={guardarDeudor}
                >
                  Guardar deudor
                </button>

              </div>

            )}


            {/* ================================= */}
            {/* FACTURAS */}
            {/* ================================= */}

            {vistaFactoring === "facturas" && (

              <div className="factoring-form">

                <h3>
                  Registrar factura
                </h3>


                <div className="asistente-campo">

                  <label>
                    Número de factura *
                  </label>

                  <input
                    type="text"
                    value={numeroFactura}
                    onChange={(e) =>
                      setNumeroFactura(
                        e.target.value
                      )
                    }
                    placeholder="Ejemplo: F001-000123"
                  />

                </div>


                <div className="asistente-campo">

                  <label>
                    RUC del cliente *
                  </label>

                  <input
                    type="text"
                    value={rucClienteFactura}
                    onChange={(e) =>
                      setRucClienteFactura(
                        e.target.value
                      )
                    }
                    placeholder="RUC del cliente"
                  />

                </div>


                <div className="asistente-campo">

                  <label>
                    RUC del deudor *
                  </label>

                  <input
                    type="text"
                    value={rucDeudorFactura}
                    onChange={(e) =>
                      setRucDeudorFactura(
                        e.target.value
                      )
                    }
                    placeholder="RUC del deudor"
                  />

                </div>


                <div className="asistente-campo">

                  <label>
                    Monto *
                  </label>

                  <input
                    type="number"
                    min="0"
                    value={montoFactura}
                    onChange={(e) =>
                      setMontoFactura(
                        e.target.value
                      )
                    }
                    placeholder="Ejemplo: 25000"
                  />

                </div>


                <div className="asistente-campo">

                  <label>
                    Moneda *
                  </label>

                  <select
                    value={monedaFactura}
                    onChange={(e) =>
                      setMonedaFactura(
                        e.target.value
                      )
                    }
                  >

                    <option value="PEN">
                      PEN
                    </option>

                    <option value="USD">
                      USD
                    </option>

                  </select>

                </div>


                <div className="asistente-campo">

                  <label>
                    Estado *
                  </label>

                  <select
                    value={estadoFactura}
                    onChange={(e) =>
                      setEstadoFactura(
                        e.target.value
                      )
                    }
                  >

                    <option value="Pendiente">
                      Pendiente
                    </option>

                    <option value="Pagada">
                      Pagada
                    </option>

                    <option value="Vencida">
                      Vencida
                    </option>

                  </select>

                </div>


                <button
                  type="button"
                  onClick={guardarFactura}
                >
                  Guardar factura
                </button>

              </div>

            )}

          </div>


          {/* ===================================== */}
          {/* PERFIL DE RIESGO */}
          {/* ===================================== */}

          <div className="configuracion-card">

            <h2>
              Perfil de riesgo
            </h2>


            {/* ================================= */}
            {/* CLIENTES VIP */}
            {/* ================================= */}

            <div className="asistente-campo">

              <label>
                ¿La empresa tiene clientes VIP? *
              </label>


              <div className="opciones-radio">

                <label>

                  <input
                    type="radio"
                    name="clientesVip"
                    value="si"
                    checked={
                      tieneClientesVip === "si"
                    }
                    onChange={(e) => {

                      setTieneClientesVip(
                        e.target.value
                      )

                      setClientesVipImportados(
                        false
                      )

                      setArchivoClientesVip(
                        null
                      )

                    }}
                  />

                  Sí

                </label>


                <label>

                  <input
                    type="radio"
                    name="clientesVip"
                    value="no"
                    checked={
                      tieneClientesVip === "no"
                    }
                    onChange={(e) => {

                      setTieneClientesVip(
                        e.target.value
                      )

                      // Si selecciona NO,
                      // ya no necesita Excel
                      setArchivoClientesVip(
                        null
                      )

                      setClientesVipImportados(
                        false
                      )

                    }}
                  />

                  No

                </label>

              </div>


              {/* ================================= */}
              {/* IMPORTACIÓN VIP */}
              {/* ================================= */}

              {tieneClientesVip === "si" && (

                <div className="clientes-vip-importacion">

                  <p className="campo-ayuda">

                    Importe un archivo Excel con
                    los clientes VIP.

                    <br />

                    El archivo debe contener:

                    <strong>
                      {" "}RUC y razón social.
                    </strong>

                  </p>


                  <label className="boton-archivo">

                    📎 Importar Excel

                    <input
                      type="file"
                      accept=".xlsx,.xls"
                      onChange={(e) => {

                        setArchivoClientesVip(
                          e.target.files[0]
                        )

                        // Al seleccionar un
                        // nuevo archivo todavía
                        // no está importado
                        setClientesVipImportados(
                          false
                        )

                      }}
                      hidden
                    />

                  </label>


                  {archivoClientesVip && (

                    <p className="archivo-seleccionado">

                      Archivo seleccionado:

                      {" "}

                      {archivoClientesVip.name}

                    </p>

                  )}


                  {clientesVipImportados && (

                    <p className="archivo-importado">

                      ✓ Clientes VIP importados
                      correctamente.

                    </p>

                  )}


                  <button
                    type="button"
                    onClick={importarClientesVip}
                    disabled={!archivoClientesVip}
                  >
                    Importar clientes VIP
                  </button>

                </div>

              )}

            </div>


            {/* ================================= */}
            {/* PÉRDIDA VIP MÍNIMA */}
            {/* ================================= */}

            <div className="asistente-campo">

              <label>
                Pérdida potencial mínima
                — cliente o área VIP *
              </label>

              <input
                type="number"
                min="0"
                value={perdidaVipMin}
                onChange={(e) =>
                  setPerdidaVipMin(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: 50000"
              />

            </div>


            {/* ================================= */}
            {/* PÉRDIDA VIP MÁXIMA */}
            {/* ================================= */}

            <div className="asistente-campo">

              <label>
                Pérdida potencial máxima
                — cliente o área VIP *
              </label>

              <input
                type="number"
                min="0"
                value={perdidaVipMax}
                onChange={(e) =>
                  setPerdidaVipMax(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: 500000"
              />

            </div>


            {/* ================================= */}
            {/* PÉRDIDA NO VIP MÍNIMA */}
            {/* ================================= */}

            <div className="asistente-campo">

              <label>
                Pérdida potencial mínima
                — cliente o área no VIP *
              </label>

              <input
                type="number"
                min="0"
                value={perdidaNoVipMin}
                onChange={(e) =>
                  setPerdidaNoVipMin(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: 10000"
              />

            </div>


            {/* ================================= */}
            {/* PÉRDIDA NO VIP MÁXIMA */}
            {/* ================================= */}

            <div className="asistente-campo">

              <label>
                Pérdida potencial máxima
                — cliente o área no VIP *
              </label>

              <input
                type="number"
                min="0"
                value={perdidaNoVipMax}
                onChange={(e) =>
                  setPerdidaNoVipMax(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: 100000"
              />

            </div>


            {/* ================================= */}
            {/* SITUACIONES RIESGOSAS */}
            {/* ================================= */}

            <div className="asistente-campo">

              <label>
                ¿Qué situaciones o solicitudes
                de correo considera especialmente
                riesgosas? *
              </label>

              <textarea
                value={situacionesRiesgosas}
                onChange={(e) =>
                  setSituacionesRiesgosas(
                    e.target.value
                  )
                }
                placeholder="Ejemplo: cambio de cuenta bancaria, modificación de datos de pago, solicitud urgente de transferencia, cambio de beneficiario"
              />

            </div>

          </div>


          {/* ===================================== */}
          {/* GUARDAR CONFIGURACIÓN */}
          {/* ===================================== */}

          <div className="guardar-configuracion">

            <button
              type="button"
              onClick={guardarConfiguracion}
            >
              Guardar configuración
            </button>

          </div>

        </div>

      )}

    </div>

  )
}

export default Configuracion
