import { useState } from 'react'
import '../App.css'
import axios from 'axios'
import * as XLSX from 'xlsx'

function Configuracion() {

  const [seccion, setSeccion] = useState("")
  const [vistaFactoring, setVistaFactoring] = useState("")

  // ==========================================
  // INFORMACIÓN DE LA ORGANIZACIÓN
  // ==========================================

  const [nombreEmpresa, setNombreEmpresa] = useState('')
  const [moneda, setMoneda] = useState('PEN')
  const [umbralMontoRelevante, setUmbralMontoRelevante] = useState('')
  const [cantidadColaboradores, setCantidadColaboradores] = useState('')
  const [areasCriticas, setAreasCriticas] = useState('')

  // ==========================================
  // USUARIOS DE ÁREAS CRÍTICAS
  // ==========================================

  const [archivoUsuarios, setArchivoUsuarios] = useState(null)
  const [usuariosExcel, setUsuariosExcel] = useState([])
  const [mostrarUsuariosExcel, setMostrarUsuariosExcel] = useState(false)

  // ==========================================
  // CONTEXTO OPERATIVO
  // ==========================================

  const [documentosRecibidosDiariamente, setDocumentosRecibidosDiariamente] = useState('')
  const [promedioOperacionesDiarias, setPromedioOperacionesDiarias] = useState('')

  // ==========================================
  // CLIENTES
  // ==========================================

  const [archivoClientes, setArchivoClientes] = useState(null)
  const [clientesExcel, setClientesExcel] = useState([])
  const [mostrarClientes, setMostrarClientes] = useState(false)

  // ==========================================
  // DEUDORES
  // ==========================================

  const [archivoDeudores, setArchivoDeudores] = useState(null)
  const [deudoresExcel, setDeudoresExcel] = useState([])
  const [mostrarDeudores, setMostrarDeudores] = useState(false)

  // ==========================================
  // PERFIL DE RIESGO
  // ==========================================

  const [perdidaVipMin, setPerdidaVipMin] = useState('')
  const [perdidaVipMax, setPerdidaVipMax] = useState('')
  const [perdidaNoVipMin, setPerdidaNoVipMin] = useState('')
  const [perdidaNoVipMax, setPerdidaNoVipMax] = useState('')
  const [situacionesRiesgosas, setSituacionesRiesgosas] = useState("")


  // ==========================================
  // LEER EXCEL DE USUARIOS
  // ==========================================

  const seleccionarExcelUsuarios = async (archivo) => {

    if (!archivo) return

    setArchivoUsuarios(archivo)
    setMostrarUsuariosExcel(false)
    setUsuariosExcel([])

    try {

      const datos = await archivo.arrayBuffer()

      const libro = XLSX.read(datos, {
        type: "array"
      })

      if (!libro.SheetNames.length) {
        throw new Error("El archivo no contiene hojas.")
      }

      const nombrePrimeraHoja = libro.SheetNames[0]
      const hoja = libro.Sheets[nombrePrimeraHoja]

      const datosExcel = XLSX.utils.sheet_to_json(
        hoja,
        {
          defval: ""
        }
      )

      setUsuariosExcel(datosExcel)

    } catch (error) {

      console.error(
        "Error al leer el Excel de usuarios:",
        error
      )

      setArchivoUsuarios(null)
      setUsuariosExcel([])

      alert(
        "No se pudo leer el archivo Excel de usuarios."
      )
    }
  }


  // ==========================================
  // LEER EXCEL DE CLIENTES
  // ==========================================

  const seleccionarExcelClientes = async (archivo) => {

    if (!archivo) return

    setArchivoClientes(archivo)
    setMostrarClientes(false)
    setClientesExcel([])

    try {

      const datos = await archivo.arrayBuffer()

      const libro = XLSX.read(datos, {
        type: "array"
      })

      if (!libro.SheetNames.length) {
        throw new Error("El archivo no contiene hojas.")
      }

      const nombrePrimeraHoja = libro.SheetNames[0]
      const hoja = libro.Sheets[nombrePrimeraHoja]

      const datosExcel = XLSX.utils.sheet_to_json(
        hoja,
        {
          defval: ""
        }
      )

      setClientesExcel(datosExcel)

    } catch (error) {

      console.error(
        "Error al leer el Excel de clientes:",
        error
      )

      setArchivoClientes(null)
      setClientesExcel([])

      alert(
        "No se pudo leer el archivo Excel de clientes."
      )
    }
  }


  // ==========================================
  // LEER EXCEL DE DEUDORES
  // ==========================================

  const seleccionarExcelDeudores = async (archivo) => {

    if (!archivo) return

    setArchivoDeudores(archivo)
    setMostrarDeudores(false)
    setDeudoresExcel([])

    try {

      const datos = await archivo.arrayBuffer()

      const libro = XLSX.read(datos, {
        type: "array"
      })

      if (!libro.SheetNames.length) {
        throw new Error("El archivo no contiene hojas.")
      }

      const nombrePrimeraHoja = libro.SheetNames[0]
      const hoja = libro.Sheets[nombrePrimeraHoja]

      const datosExcel = XLSX.utils.sheet_to_json(
        hoja,
        {
          defval: ""
        }
      )

      setDeudoresExcel(datosExcel)

    } catch (error) {

      console.error(
        "Error al leer el Excel de deudores:",
        error
      )

      setArchivoDeudores(null)
      setDeudoresExcel([])

      alert(
        "No se pudo leer el archivo Excel de deudores."
      )
    }
  }


  // ==========================================
  // GUARDAR CONFIGURACIÓN
  // ==========================================

  const guardarConfiguracion = async () => {

    // ------------------------------------------
    // VALIDAR CAMPOS
    // ------------------------------------------

    if (
      !nombreEmpresa ||
      !moneda ||
      !umbralMontoRelevante ||
      !cantidadColaboradores ||
      !areasCriticas ||
      !documentosRecibidosDiariamente ||
      !promedioOperacionesDiarias ||
      !perdidaVipMin ||
      !perdidaVipMax ||
      !perdidaNoVipMin ||
      !perdidaNoVipMax ||
      !situacionesRiesgosas
    ) {

      alert(
        "Completa todos los campos obligatorios antes de guardar."
      )

      return
    }


    // ------------------------------------------
    // VALIDAR PÉRDIDA VIP
    // ------------------------------------------

    if (
      Number(perdidaVipMax) <
      Number(perdidaVipMin)
    ) {

      alert(
        "La pérdida potencial máxima para clientes VIP no puede ser menor que la mínima."
      )

      return
    }


    // ------------------------------------------
    // VALIDAR PÉRDIDA NO VIP
    // ------------------------------------------

    if (
      Number(perdidaNoVipMax) <
      Number(perdidaNoVipMin)
    ) {

      alert(
        "La pérdida potencial máxima para clientes no VIP no puede ser menor que la mínima."
      )

      return
    }


    // ------------------------------------------
    // VALIDAR USUARIOS DEL EXCEL
    // ------------------------------------------

    const usuariosParaGuardar =
      usuariosExcel.map((usuario) => ({

        nombre: String(
          usuario.nombre ??
          usuario.Nombre ??
          ""
        ).trim(),

        correo: String(
          usuario.correo ??
          usuario.Correo ??
          ""
        ).trim(),

        area: String(
          usuario.area ??
          usuario.Area ??
          ""
        ).trim()

      }))


    if (
      usuariosParaGuardar.length === 0 ||
      usuariosParaGuardar.some(
        (usuario) =>
          !usuario.nombre ||
          !usuario.correo ||
          !usuario.area
      )
    ) {

      alert(
        "El Excel de usuarios debe contener las columnas nombre, correo y area, y todos los registros deben tener datos."
      )

      return
    }


    try {

      // ==========================================
      // 1. GUARDAR CONFIGURACIÓN GENERAL
      // ==========================================

      const respuestaConfiguracion = await axios.post(
        "http://localhost:8080/configuracion",
        {

          nombreEmpresa,

          moneda,

          umbralMontoRelevante:
            Number(
              umbralMontoRelevante
            ),

          cantidadColaboradores:
            Number(
              cantidadColaboradores
            ),

          areasCriticas,

          documentosRecibidosDiariamente,

          promedioOperacionesDiarias:
            Number(
              promedioOperacionesDiarias
            ),

          perdidaMinVip:
            Number(
              perdidaVipMin
            ),

          perdidaMaxVip:
            Number(
              perdidaVipMax
            ),

          perdidaMinNoVip:
            Number(
              perdidaNoVipMin
            ),

          perdidaMaxNoVip:
            Number(
              perdidaNoVipMax
            ),

          situacionesRiesgosas

        }
      )


      const configuracionEmpresaId =
        respuestaConfiguracion.data.id


      // ==========================================
      // 2. GUARDAR USUARIOS CRÍTICOS
      // ==========================================

      for (
        const usuario of usuariosParaGuardar
      ) {

        await axios.post(
          "http://localhost:8080/usuarios-criticos",
          {

            nombre: usuario.nombre,

            correo: usuario.correo,

            area: usuario.area,

            configuracionEmpresa: {
              id: configuracionEmpresaId
            }

          }
        )
      }


      // ==========================================
      // 3. GUARDAR CLIENTES
      // ==========================================

      if (archivoClientes) {

        const datosClientes =
          new FormData()

        datosClientes.append(
          "archivo",
          archivoClientes
        )

        datosClientes.append(
          "configuracionEmpresaId",
          configuracionEmpresaId
        )

        await axios.post(
          "http://localhost:8080/clientes/importar-vip",
          datosClientes,
          {
            headers: {
              "Content-Type":
                "multipart/form-data"
            }
          }
        )
      }


      // ==========================================
      // 4. GUARDAR DEUDORES
      // ==========================================

      if (archivoDeudores) {

        const datosDeudores =
          new FormData()

        datosDeudores.append(
          "archivo",
          archivoDeudores
        )

        datosDeudores.append(
            "configuracionEmpresaId",
            configuracionEmpresaId
        )

        await axios.post(
          "http://localhost:8080/deudores/importar",
          datosDeudores,
          {
            headers: {
              "Content-Type":
                "multipart/form-data"
            }
          }
        )
      }


      // ==========================================
      // ÉXITO
      // ==========================================

      alert(
        "Configuración guardada correctamente."
      )

    } catch (error) {

      console.error(
        "Error al guardar configuración:",
        error
      )

      console.error(
        "Respuesta del servidor:",
        error.response?.data
      )

      alert(
        "No se pudo guardar la configuración."
      )
    }
  }


  // ==========================================
  // INTERFAZ
  // ==========================================

  return (

    <div className="configuracion-container">


      {/* ======================================
          ENCABEZADO
      ======================================= */}

      <div className="configuracion-header">

        <h1>
          Configuración
        </h1>

        <p>
          Configure la información de su
          organización y los parámetros
          utilizados para la evaluación
          de riesgos.
        </p>

      </div>


      {/* ======================================
          PESTAÑAS PRINCIPALES
      ======================================= */}

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


      {/* ======================================
          TARJETA PRINCIPAL
      ======================================= */}

      <div className="configuracion-card">


        {/* ====================================
            INTEGRACIONES
        ===================================== */}

        {seccion === "integraciones" && (

          <div className="asistente-datos">

            <div className="asistente-header">

              <h2>
                Integraciones
              </h2>

              <p>
                Conecte las herramientas
                utilizadas por su organización.
              </p>

            </div>


            <div className="asistente-campo">

              <label>
                Microsoft 365
              </label>

              <button>
                Conectar
              </button>

            </div>


            <div className="asistente-campo">

              <label>
                Google Workspace
              </label>

              <button>
                Conectar
              </button>

            </div>

          </div>

        )}


        {/* ====================================
            USUARIOS Y ROLES
        ===================================== */}

        {seccion === "usuarios" && (

          <div className="asistente-datos">

            <div className="asistente-header">

              <h2>
                Usuarios y Roles
              </h2>

              <p>
                La administración de usuarios
                se encuentra disponible desde
                el módulo correspondiente.
              </p>

            </div>

          </div>

        )}


        {/* ====================================
            ASISTENTE DE DATOS
        ===================================== */}

        {seccion === "asistente" && (

          <div className="asistente-datos">


            <div className="asistente-header">

              <h2>
                Asistente de datos
              </h2>

              <p>
                Configure los datos necesarios
                para evaluar los riesgos de
                ciberseguridad de la organización.
              </p>

            </div>


            {/* ==================================
                INFORMACIÓN DE LA ORGANIZACIÓN
            =================================== */}

            <h3>
              Información de la organización
            </h3>


            <div className="asistente-campo">

              <label>
                Nombre de la empresa *
              </label>

              <input
                type="text"
                value={nombreEmpresa}
                onChange={(e) =>
                  setNombreEmpresa(
                    e.target.value
                  )
                }
                placeholder="Ingrese el nombre de la empresa"
              />

            </div>


            <div className="asistente-campo">

              <label>
                Moneda principal *
              </label>

              <select
                value={moneda}
                onChange={(e) =>
                  setMoneda(
                    e.target.value
                  )
                }
              >

                <option value="PEN">
                  Soles (PEN)
                </option>

                <option value="USD">
                  Dólares (USD)
                </option>

              </select>

            </div>


            <div className="asistente-campo">

              <label>
                Umbral de monto relevante *
              </label>

              <input
                type="number"
                value={umbralMontoRelevante}
                onChange={(e) =>
                  setUmbralMontoRelevante(
                    e.target.value
                  )
                }
                placeholder="Ej. 50000"
              />

            </div>


            <div className="asistente-campo">

              <label>
                Cantidad de colaboradores *
              </label>

              <input
                type="number"
                value={cantidadColaboradores}
                onChange={(e) =>
                  setCantidadColaboradores(
                    e.target.value
                  )
                }
                placeholder="Ej. 50"
              />

            </div>


            <div className="asistente-campo">

              <label>
                ¿Cuál es el área crítica
                de la organización? *
              </label>

              <select
                value={areasCriticas}
                onChange={(e) =>
                  setAreasCriticas(
                    e.target.value
                  )
                }
              >

                <option value="">
                  Seleccione un área
                </option>

                <option value="Finanzas">
                  Finanzas
                </option>

                <option value="Contabilidad">
                  Contabilidad
                </option>

                <option value="Tesorería">
                  Tesorería
                </option>

                <option value="Operaciones">
                  Operaciones
                </option>

                <option value="TI">
                  Tecnología de la Información
                </option>

                <option value="Riesgos">
                  Riesgos
                </option>

                <option value="Créditos">
                  Créditos
                </option>

                <option value="Cobranzas">
                  Cobranzas
                </option>

                <option value="Administración">
                  Administración
                </option>

                <option value="Legal">
                  Legal
                </option>

              </select>

            </div>


            {/* ==================================
                USUARIOS DE ÁREAS CRÍTICAS
            =================================== */}

            <h3>
              Usuarios de áreas críticas
            </h3>


            <div className="asistente-campo">

              <label>
                Cargar usuarios desde Excel *
              </label>


              <input
                type="file"
                accept=".xlsx,.xls"
                id="excel-usuarios"
                onChange={(e) =>
                  seleccionarExcelUsuarios(
                    e.target.files[0]
                  )
                }
                hidden
              />


              <label
                htmlFor="excel-usuarios"
                className="boton-secundario"
              >
                Seleccionar Excel
              </label>


              {archivoUsuarios && (

                <p>

                  Archivo seleccionado:{" "}

                  <strong>
                    {archivoUsuarios.name}
                  </strong>

                </p>

              )}

            </div>


            {/* BOTÓN VISUALIZAR USUARIOS */}

            {usuariosExcel.length > 0 && (

              <div className="asistente-campo">

                <button
                  type="button"
                  className="boton-secundario"
                  onClick={() =>
                    setMostrarUsuariosExcel(
                      !mostrarUsuariosExcel
                    )
                  }
                >

                  {mostrarUsuariosExcel
                    ? "Ocultar datos"
                    : "Visualizar datos"}

                </button>

              </div>

            )}


            {/* TABLA USUARIOS */}

            {mostrarUsuariosExcel &&
              usuariosExcel.length > 0 && (

                <div className="clientes-tabla-container">

                  <table className="clientes-tabla">

                    <thead>

                      <tr>

                        <th>
                          Nombre
                        </th>

                        <th>
                          Correo
                        </th>

                        <th>
                          Área
                        </th>

                      </tr>

                    </thead>


                    <tbody>

                      {usuariosExcel.map(
                        (usuario, indice) => {

                          const nombre =
                            usuario.nombre ??
                            usuario.Nombre ??
                            "-"

                          const correo =
                            usuario.correo ??
                            usuario.Correo ??
                            "-"

                          const area =
                            usuario.area ??
                            usuario.Area ??
                            "-"


                          return (

                            <tr key={indice}>

                              <td>
                                {nombre}
                              </td>

                              <td>
                                {correo}
                              </td>

                              <td>
                                {area}
                              </td>

                            </tr>

                          )

                        }
                      )}

                    </tbody>

                  </table>

                </div>

              )}


            {/* ==================================
                CONTEXTO OPERATIVO
            =================================== */}

            <h3>
              Contexto operativo
            </h3>


            <div className="asistente-campo">

              <label>
                ¿Qué tipo de documentos
                recibe diariamente? *
              </label>


              <select
                value={
                  documentosRecibidosDiariamente
                }
                onChange={(e) =>
                  setDocumentosRecibidosDiariamente(
                    e.target.value
                  )
                }
              >

                <option value="">
                  Seleccione un tipo de documento
                </option>

                <option value="Facturas">
                  Facturas
                </option>

                <option value="Órdenes de compra">
                  Órdenes de compra
                </option>

                <option value="Contratos">
                  Contratos
                </option>

                <option value="Estados de cuenta">
                  Estados de cuenta
                </option>

                <option value="Comprobantes de pago">
                  Comprobantes de pago
                </option>

                <option value="Documentos de clientes">
                  Documentos de clientes
                </option>

                <option value="Documentos de deudores">
                  Documentos de deudores
                </option>

                <option value="Otros documentos financieros">
                  Otros documentos financieros
                </option>

              </select>

            </div>


            <div className="asistente-campo">

              <label>
                Promedio de operaciones diarias *
              </label>

              <input
                type="number"
                value={
                  promedioOperacionesDiarias
                }
                onChange={(e) =>
                  setPromedioOperacionesDiarias(
                    e.target.value
                  )
                }
                placeholder="Ej. 100"
              />

            </div>


            {/* ==================================
                CONTEXTO FACTORING
            =================================== */}

            <h3>
              Contexto factoring
            </h3>


            <div className="configuracion-tabs">

              <button
                className={
                  vistaFactoring === "clientes"
                    ? "tab-activa"
                    : ""
                }
                onClick={() =>
                  setVistaFactoring(
                    "clientes"
                  )
                }
              >
                Clientes
              </button>


              <button
                className={
                  vistaFactoring === "deudores"
                    ? "tab-activa"
                    : ""
                }
                onClick={() =>
                  setVistaFactoring(
                    "deudores"
                  )
                }
              >
                Deudores
              </button>

            </div>


            {/* ==================================
                CLIENTES
            =================================== */}

            {vistaFactoring === "clientes" && (

              <div className="asistente-campo">

                <label>
                  Clientes
                </label>


                <input
                  type="file"
                  accept=".xlsx,.xls"
                  id="excel-clientes"
                  onChange={(e) =>
                    seleccionarExcelClientes(
                      e.target.files[0]
                    )
                  }
                  hidden
                />


                <label
                  htmlFor="excel-clientes"
                  className="boton-secundario"
                >
                  Seleccionar Excel
                </label>


                {archivoClientes && (

                  <p>

                    Archivo seleccionado:{" "}

                    <strong>
                      {archivoClientes.name}
                    </strong>

                  </p>

                )}


                {clientesExcel.length > 0 && (

                  <button
                    type="button"
                    className="boton-secundario"
                    onClick={() =>
                      setMostrarClientes(
                        !mostrarClientes
                      )
                    }
                  >

                    {mostrarClientes
                      ? "Ocultar datos"
                      : "Visualizar datos"}

                  </button>

                )}


                {mostrarClientes &&
                  clientesExcel.length > 0 && (

                    <div className="clientes-tabla-container">

                      <table className="clientes-tabla">

                        <thead>

                          <tr>

                            {Object.keys(
                              clientesExcel[0]
                            ).map(
                              (columna) => (

                                <th
                                  key={columna}
                                >
                                  {columna}
                                </th>

                              )
                            )}

                          </tr>

                        </thead>


                        <tbody>

                          {clientesExcel.map(
                            (cliente, indice) => (

                              <tr
                                key={indice}
                              >

                                {Object.values(
                                  cliente
                                ).map(
                                  (
                                    valor,
                                    index
                                  ) => (

                                    <td
                                      key={index}
                                    >
                                      {String(
                                        valor ??
                                          ""
                                      )}
                                    </td>

                                  )
                                )}

                              </tr>

                            )
                          )}

                        </tbody>

                      </table>

                    </div>

                  )}

              </div>

            )}


            {/* ==================================
                DEUDORES
            =================================== */}

            {vistaFactoring === "deudores" && (

              <div className="asistente-campo">

                <label>
                  Deudores
                </label>


                <input
                  type="file"
                  accept=".xlsx,.xls"
                  id="excel-deudores"
                  onChange={(e) =>
                    seleccionarExcelDeudores(
                      e.target.files[0]
                    )
                  }
                  hidden
                />


                <label
                  htmlFor="excel-deudores"
                  className="boton-secundario"
                >
                  Seleccionar Excel
                </label>


                {archivoDeudores && (

                  <p>

                    Archivo seleccionado:{" "}

                    <strong>
                      {archivoDeudores.name}
                    </strong>

                  </p>

                )}


                {deudoresExcel.length > 0 && (

                  <button
                    type="button"
                    className="boton-secundario"
                    onClick={() =>
                      setMostrarDeudores(
                        !mostrarDeudores
                      )
                    }
                  >

                    {mostrarDeudores
                      ? "Ocultar datos"
                      : "Visualizar datos"}

                  </button>

                )}


                {mostrarDeudores &&
                  deudoresExcel.length > 0 && (

                    <div className="clientes-tabla-container">

                      <table className="clientes-tabla">

                        <thead>

                          <tr>

                            {Object.keys(
                              deudoresExcel[0]
                            ).map(
                              (columna) => (

                                <th
                                  key={columna}
                                >
                                  {columna}
                                </th>

                              )
                            )}

                          </tr>

                        </thead>


                        <tbody>

                          {deudoresExcel.map(
                            (deudor, indice) => (

                              <tr
                                key={indice}
                              >

                                {Object.values(
                                  deudor
                                ).map(
                                  (
                                    valor,
                                    index
                                  ) => (

                                    <td
                                      key={index}
                                    >
                                      {String(
                                        valor ??
                                          ""
                                      )}
                                    </td>

                                  )
                                )}

                              </tr>

                            )
                          )}

                        </tbody>

                      </table>

                    </div>

                  )}

              </div>

            )}


            {/* ==================================
                PERFIL DE RIESGO
            =================================== */}

            <h3>
              Perfil de riesgo
            </h3>


            <div className="asistente-campo">

              <label>
                Pérdida potencial mínima —
                cliente o área VIP *
              </label>

              <input
                type="number"
                value={perdidaVipMin}
                onChange={(e) =>
                  setPerdidaVipMin(
                    e.target.value
                  )
                }
                placeholder="Ej. 10000"
              />

            </div>


            <div className="asistente-campo">

              <label>
                Pérdida potencial máxima —
                cliente o área VIP *
              </label>

              <input
                type="number"
                value={perdidaVipMax}
                onChange={(e) =>
                  setPerdidaVipMax(
                    e.target.value
                  )
                }
                placeholder="Ej. 100000"
              />

            </div>


            <div className="asistente-campo">

              <label>
                Pérdida potencial mínima —
                cliente o área no VIP *
              </label>

              <input
                type="number"
                value={perdidaNoVipMin}
                onChange={(e) =>
                  setPerdidaNoVipMin(
                    e.target.value
                  )
                }
                placeholder="Ej. 1000"
              />

            </div>


            <div className="asistente-campo">

              <label>
                Pérdida potencial máxima —
                cliente o área no VIP *
              </label>

              <input
                type="number"
                value={perdidaNoVipMax}
                onChange={(e) =>
                  setPerdidaNoVipMax(
                    e.target.value
                  )
                }
                placeholder="Ej. 10000"
              />

            </div>


            {/* ==================================
                SITUACIONES RIESGOSAS
            =================================== */}

            <div className="asistente-campo">

              <label>
                ¿Qué situaciones o solicitudes
                de correo considera especialmente
                riesgosas? *
              </label>


              <select
                value={situacionesRiesgosas}
                onChange={(e) =>
                  setSituacionesRiesgosas(
                    e.target.value
                  )
                }
              >

                <option value="">
                  Seleccione una situación
                </option>

                <option value="Cambio de cuenta bancaria">
                  Cambio de cuenta bancaria
                </option>

                <option value="Cambio de beneficiario">
                  Cambio de beneficiario
                </option>

                <option value="Modificación de datos de pago">
                  Modificación de datos de pago
                </option>

                <option value="Solicitud urgente de transferencia">
                  Solicitud urgente de transferencia
                </option>

                <option value="Solicitud de información confidencial">
                  Solicitud de información confidencial
                </option>

                <option value="Envío de documentos sensibles">
                  Envío de documentos sensibles
                </option>

                <option value="Solicitud de desembolso">
                  Solicitud de desembolso
                </option>

              </select>

            </div>


            {/* ==================================
                GUARDAR
            =================================== */}

            <div className="asistente-campo">

              <button
                type="button"
                onClick={
                  guardarConfiguracion
                }
              >
                Guardar configuración
              </button>

            </div>

          </div>

        )}

      </div>

    </div>
  )
}

export default Configuracion
