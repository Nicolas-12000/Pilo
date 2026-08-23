import Link from "next/link";

export default function Home() {
  return (
    <main className="flex flex-1 flex-col justify-center px-4 py-16">
      <div className="mx-auto w-full max-w-3xl">
        <p className="font-caps text-secondary">PILO</p>
        <h1 className="mt-3 font-display text-on-surface">
          Trámites administrativos, de extremo a extremo.
        </h1>
        <p className="mt-4 text-body-lg text-on-surface-variant">
          Explora el catálogo de trámites, revisa requisitos y plazos sin crear cuenta.
          Solo necesitarás iniciar sesión cuando quieras abrir un expediente.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link
            href="/tramites"
            className="inline-flex h-12 items-center rounded-md bg-primary px-5 font-label text-on-primary hover:bg-primary-hover"
          >
            Ver trámites
          </Link>
          <Link
            href="/login"
            className="inline-flex h-12 items-center rounded-md bg-surface px-5 font-label text-on-surface ring-1 ring-outline-variant hover:bg-surface-container-high"
          >
            Iniciar sesión
          </Link>
        </div>
      </div>
    </main>
  );
}
