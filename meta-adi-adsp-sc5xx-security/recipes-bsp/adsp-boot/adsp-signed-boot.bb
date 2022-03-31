DESCRIPTION = "Generate signed boot files for ADSP platforms"
LICENSE = "CLOSED"
# todo license files

inherit deploy deploy-dep

DDEPENDS = "u-boot-adi adsp-boot"

# overwrite with another signature type in local.conf if you prefer
# valid options are:
# BLp - plaintext with authentication
# BLw - encrypted in wrapped key format
# BLx - encrypted in keyless format
ADI_SIGNATURE_TYPE ?= "BLp"

SIGNTOOL_PROC = "ADSP-SC594"
SIGNTOOL_PROC_adsp-sc598-som-ezkit = "ADSP-SC598"

SIGNTOOL_ALGO = "ecdsa256"

FILES_${PN} = "\
	stage1-boot.ldr \
	stage2-boot.ldr \
"

DEPLOY_SRC_URI = "stage1-boot-unsigned.ldr stage2-boot-unsigned.ldr"

do_configure() {
	if [ -z "${ADI_SIGNTOOL_KEY}" ]; then
		bbfatal "Signing key not specified, please set ADI_SIGNTOOL_KEY in local.conf"
	fi

	if [ ! -f "${ADI_SIGNTOOL_PATH}" ]; then
		bbfatal "Must specify a path to the adi_signtool binary as ADI_SIGNTOOL_PATH in local.conf"
	fi

	if [ ! -f "${ADI_SIGNTOOL_KEY}" ]; then
		bbfatal "Signing key '${ADI_SIGNTOOL_KEY}' not found"
	fi
}

do_compile() {
	cd ${WORKDIR}

	${ADI_SIGNTOOL_PATH} -proc ${SIGNTOOL_PROC} sign -type ${ADI_SIGNATURE_TYPE} -algo ${SIGNTOOL_ALGO} \
		-attribute 0x80000002=${LDR_BCODE} \
		-infile stage1-boot-unsigned.ldr -outfile stage1-boot.ldr \
		-prikey ${ADI_SIGNTOOL_KEY}

	${ADI_SIGNTOOL_PATH} -proc ${SIGNTOOL_PROC} sign -type ${ADI_SIGNATURE_TYPE} -algo ${SIGNTOOL_ALGO} \
		-attribute 0x80000002=${LDR_BCODE} \
		-infile stage2-boot-unsigned.ldr -outfile stage2-boot.ldr \
		-prikey ${ADI_SIGNTOOL_KEY}
}

do_install() {
	install -m 0755 ${WORKDIR}/stage1-boot.ldr ${D}/
	install -m 0755 ${WORKDIR}/stage2-boot.ldr ${D}/
}

do_deploy() {
	install -m 0755 ${WORKDIR}/stage1-boot.ldr ${DEPLOYDIR}/
	install -m 0755 ${WORKDIR}/stage2-boot.ldr ${DEPLOYDIR}/
}

addtask do_deploy after do_compile before do_install
